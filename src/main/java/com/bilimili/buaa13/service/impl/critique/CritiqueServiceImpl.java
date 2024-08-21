package com.bilimili.buaa13.service.impl.critique;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bilimili.buaa13.entity.*;
import com.bilimili.buaa13.im.IMServer;
import com.bilimili.buaa13.mapper.ArticleMapper;
import com.bilimili.buaa13.mapper.CritiqueMapper;
import com.bilimili.buaa13.service.article.ArticleStatusService;
import com.bilimili.buaa13.service.critique.CritiqueService;
import com.bilimili.buaa13.service.message.MessageUnreadService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.tools.RedisTool;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Stream;



//修改于2024.08.11
//by zmh


@Slf4j
@Service
public class CritiqueServiceImpl implements CritiqueService {

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private CritiqueMapper critiqueMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleStatusService articleStatusService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageUnreadService messageUnreadService;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    //----------------------------------------------------------------------------
    //基于原来思路的额外修改
    @Override
    @Transactional
    public ResponseResult deleteCritiqueFromArticle(Integer criId, Integer postId, boolean isAdmin) {
        // 初始化响应对象
        ResponseResult responseResult = new ResponseResult();

        // 查询评论是否存在且未被删除
        Critique critique = critiqueMapper.selectOne(new QueryWrapper<Critique>()
                .eq("criId", criId)
                .eq("is_deleted", 0));

        if (critique == null) {
            // 评论不存在
            return new ResponseResult(404, "评论不存在",null);
        }

        // 获取文章信息
        Article article = articleMapper.selectById(critique.getAid());

        // 判断删除权限：管理员、本评论的发布者、文章作者
        if (isAdmin || critique.getPostId().equals(postId) || article.getUid().equals(postId)) {
            // 标记该评论为已删除
            critiqueMapper.update(null, new UpdateWrapper<Critique>()
                    .eq("criId", criId)
                    .set("is_deleted", 1));

            // 更新文章统计数据
            articleStatusService.updateArticleStatus(critique.getAid(), "critique", false, 1);

            // 递归删除所有子评论
            deleteChildCritiques(criId, postId);

            // 删除成功
            return new ResponseResult(200, "删除成功!",null);
        } else {
            // 无权删除
            return new ResponseResult(403, "你无权删除该条评论",null);
        }
    }

    private void deleteChildCritiques(Integer rootCriId, Integer postId) {
        List<Critique> childCritiques = getChildCritiquesByRootId(rootCriId, 0L, -1L);

        if (childCritiques != null && !childCritiques.isEmpty()) {
            for (Critique child : childCritiques) {
                critiqueMapper.update(null, new UpdateWrapper<Critique>()
                        .eq("criId", child.getCriId())
                        .set("is_deleted", 1));
                deleteChildCritiques(child.getCriId(), postId);
            }
        }
    }

    //----------------------------------------------------------------------------------

    /**
     * 获取评论树列表
     * @param aid   对应文章ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param sortType  排序类型 1 按热度排序 2 按时间排序
     * @return  评论树列表
     * 2024.08.05
     */
    @Override
    public List<CritiqueTree> getCritiqueTreeByAid(Integer aid, Long offset, Integer sortType) {
        // 查询父级评论
        List<Critique> critiquesRoot = getRootCritiquesByAid(aid, offset, sortType);
        List<CritiqueTree> critiqueTrees = new ArrayList<>();
        for (Critique critique : critiquesRoot) {
            CritiqueTree critiqueTree = buildCritiqueTree(critique,0L,2L);
            critiqueTrees.add(critiqueTree);
        }
        return critiqueTrees;
    }

    /**
     * 构建评论树
     * @param critique 根评论
     * @param start 子评论开始偏移量
     * @param end  子评论结束偏移量
     * @return  单棵评论树
     * 2024.08.03
     */
    private CritiqueTree buildCritiqueTree(Critique critique, Long start, Long end) {
        //先将子节点设为null，下面再递归构建
        CritiqueTree critiqueTree = new CritiqueTree(
                critique.getCriId(),
                critique.getAid(),
                critique.getRootCid(),
                critique.getParentCid(),
                critique.getContent(),
                userService.getUserByUId(critique.getPostId()),
                userService.getUserByUId(critique.getAcceptId()),
                critique.getUpVote(),
                critique.getDownVote(),
                null,
                critique.getCreateTime(),
                0L
        );

        // 递归查询构建子评论树
        Integer criId = critique.getCriId();
        QueryWrapper<Critique> critiqueQueryWrapper = new QueryWrapper<>();
        critiqueQueryWrapper.eq("parent_id", criId).ne("is_deleted", 1);
        long count = critiqueMapper.selectCount(critiqueQueryWrapper);
        critiqueTree.setCount(count);
        List<Critique> critiques;
        if(end.equals(-1L)){
            end = Long.MAX_VALUE;
        }
        long limit = end - start +1;
        long currentPage = start / limit + 1;
        Page<Critique> page = new Page<>(currentPage, limit);
        Page<Critique> critiquePage = critiqueMapper.selectPage(page, critiqueQueryWrapper);
        //查询回复的评论数组
        critiques = critiquePage.getRecords();
        List<Critique> sonCritiques = getChildCritiquesByRootId(critique.getCriId(), start, end);
        if (critique.getRootCid() == 0 || (critiques!=null && !critiques.isEmpty()) || (sonCritiques !=null && !sonCritiques.isEmpty())) {
            List<CritiqueTree> sonTreeList = new ArrayList<>();
            if(sonCritiques ==null || sonCritiques.isEmpty()){ sonCritiques = critiques;}
            for(Critique sonCritique : sonCritiques) {
                CritiqueTree sonNode = buildCritiqueTree(sonCritique, start, end);
                sonTreeList.add(sonNode);
            }
            critiqueTree.setSonNode(sonTreeList);
        }
        return critiqueTree;
    }

    /**
     * 发送评论，字数不得大于2000或为空
     * @param aid   文章id
     * @param postId   发布者postId
     * @param rootId    楼层id（根评论id）
     * @param parentId  被回复的评论id
     * @param acceptId  被回复用户postId
     * @param content   评论内容
     * @return  true 发送成功 false 发送失败
     * 2024.08.05
     */
    @Override
    @Transactional
    public CritiqueTree sendCritique(Integer aid, Integer postId, Integer rootId, Integer parentId, Integer acceptId, String content) {
        if (content == null || content.isEmpty() || content.length() > 2000) return null;
        Critique critique = new Critique(
                null,
                aid,
                postId,
                rootId,
                parentId,
                acceptId,
                content,
                0,
                0,
                new Date(),
                null,
                null
        );
        critiqueMapper.insert(critique);
        // 更新文章评论 + 1
        articleStatusService.updateArticleStatus(critique.getAid(), "critique", true, 1);

        CritiqueTree critiqueTree = buildCritiqueTree(critique, 0L, -1L);

        try {
            //1注释Redis
            // 如果不是根级评论，则加入 redis 对应的 storeZSet 中
            if (!rootId.equals(0)) {
                redisTool.storeZSet("critique_reply:" + rootId, critique.getCriId());
            } else {
                redisTool.storeZSet("critique_article:"+ aid, critique.getCriId());
            }
            // 表示被回复的用户收到的回复评论的 criId 有序集合
            // 如果不是回复自己
            if(!critique.getAcceptId().equals(critique.getPostId())) {
                //1注释Redis
                redisTool.storeZSet("reply_zset:" + critique.getAcceptId(), critique.getCriId());
                messageUnreadService.addOneUnread(critique.getAcceptId(), "reply");

                // 通知未读消息
                Map<String, Object> map = new HashMap<>();
                map.put("type", "接收");
                Set<Channel> critiqueChannel = IMServer.userChannel.get(critique.getAcceptId());
                if (critiqueChannel != null) {
                    critiqueChannel.stream().parallel().forEach(channel -> channel.writeAndFlush(IMResponse.message("reply", map)));
                }
            }
        } catch (Exception e) {
            log.error("评论出错了\n{}", e.getMessage());
        }

        return critiqueTree;
    }

    /**
     * 删除评论
     * @param criId    评论id
     * @param postId   当前用户id
     * @param isAdmin   是否是管理员
     * @return  响应对象
     */
    @Override
    @Transactional
    public ResponseResult deleteCritique(Integer criId, Integer postId, boolean isAdmin) {
        ResponseResult responseResult = new ResponseResult();
        QueryWrapper<Critique> critiqueQueryWrapper = new QueryWrapper<>();
        critiqueQueryWrapper.eq("criId", criId).ne("is_deleted", 1);
        Critique critique = critiqueMapper.selectOne(critiqueQueryWrapper);
        if (critique == null) {
            responseResult.setCode(404);
            responseResult.setMessage("评论不存在");
            return responseResult;
        }

        // 限制评论只能由本人或管理员或作者删除
        Article article = articleMapper.selectById(critique.getAid());
        if (critique.getPostId().equals(postId) || isAdmin || article.getUid().equals(postId)) {
            // 删除评论
            UpdateWrapper<Critique> deleteCritiqueWrapper = new UpdateWrapper<>();
            deleteCritiqueWrapper.eq("criId", critique.getCriId()).set("is_deleted", 1);
            critiqueMapper.update(null, deleteCritiqueWrapper);
            articleStatusService.updateArticleStatus(critique.getAid(), "critique", false, 1);
            //并行流递归删除子评论
            List<Critique> childCritiques = getChildCritiquesByRootId(criId,0L,-1L);
            if (childCritiques == null || childCritiques.isEmpty()) {
                responseResult.setMessage("已删除完毕");
                return responseResult;
            }
            List<ResponseResult> responseResults = childCritiques.stream().parallel().flatMap(
                    childCritique ->{
                        ResponseResult childResponse = new ResponseResult();
                        childResponse = deleteCritique(childCritique.getCriId(),postId,true);
                        return Stream.of(childResponse);
                    }
            ).toList();


            responseResult.setCode(200);
            responseResult.setMessage("删除成功!");
        } else {
            responseResult.setCode(403);
            responseResult.setMessage("你无权删除该条评论");
        }
        return responseResult;
    }

    /**
     * @param rootId 根级节点的评论 criId, 即楼层 criId
     * @return 1. 根据 redis 查找出回复该评论的子评论 criId 列表
     * 2. 根据 criId 多线程查询出所有评论的详细信息
     * 2024.08.03
     */
    @Override
    public List<Critique> getChildCritiquesByRootId(Integer rootId, Long start, Long end) {
        //1注释Redis
        Set<Object> replyIds = redisTemplate.opsForZSet().range("critique_reply:" + rootId, start, end);
        if (replyIds == null || replyIds.isEmpty()) return Collections.emptyList();
        QueryWrapper<Critique> wrapper = new QueryWrapper<>();
        wrapper.in("id", replyIds).ne("is_deleted", 1);
        //return critiqueMapper.selectList(wrapper);
        if(end.equals(-1L)){
            return critiqueMapper.getRootCritiqueByStartNoLimit(rootId, start);
        }
        return critiqueMapper.getRootCritiquesByStartAndLimit(rootId, start, end - start +1);
    }

    /**
     * 根据文章 aid 获取根评论列表，一次查 10 条
     * @param aid 文章 criId
     * @param offset 偏移量，已经获取到的根评论数量
     * @param sortType 1:按热度排序 2:按时间排序
     * @return List<Critique>
     * 2024.08.05
     */
    @Override
    public List<Critique> getRootCritiquesByAid(Integer aid, Long offset, Integer sortType) {
        //1注释Redis
        Set<Object> rootIdsSet;
        if (sortType == 1) {
            // 按热度排序就不能用时间分数查偏移量了，要全部查出来，后续在MySQL筛选
            rootIdsSet = redisTool.reverseRange("critique_article:" + aid, 0L, -1L);
        } else {
            rootIdsSet = redisTool.reverseRange("critique_article:" + aid, offset, offset + 9L);
        }

        if (rootIdsSet == null || rootIdsSet.isEmpty()) return Collections.emptyList();

        QueryWrapper<Critique> critiqueQueryWrapper = new QueryWrapper<>();
        critiqueQueryWrapper.in("id", rootIdsSet).ne("is_deleted", 1);
        if (sortType == 1) { // 热度
            critiqueQueryWrapper.orderByDesc("(love - bad)").last("LIMIT 10 OFFSET " + offset);
        } else if(sortType == 2){ // 时间
            critiqueQueryWrapper.orderByDesc("create_time");
        }
        //return critiqueMapper.selectList(critiqueQueryWrapper);
        if(sortType == 1) return critiqueMapper.getAidRootCritiquesByHeat(aid,offset,10L);
        else if (sortType == 2) return critiqueMapper.getAidRootCritiquesByTime(aid,offset,10L);
        else return Collections.emptyList();
    }

    /**
     * 获取更多回复评论
     * 由于获取的是回复，所以根据当前评论节点为基准，构建评论树
     * @param criId 根评论id
     * @return  包含全部回复评论的评论树
     * 2024.08.03
     */
    @Override
    public CritiqueTree getMoreCritiquesById(Integer criId) {
        Critique critique = critiqueMapper.selectById(criId);
        return buildCritiqueTree(critique, 0L, -1L);
    }

    /**
     * 同时相对更新点赞和点踩
     * 用于原本点踩了，现在直接点赞，一次改完。
     * @param criId    评论id
     * @param addUpVote   true 点赞 false 点踩
     * 2024.08.03
     */
    @Override
    public void updateLikeAndDisLike(Integer criId, boolean addUpVote) {
        UpdateWrapper<Critique> updateWrapper = new UpdateWrapper<>();
        if (addUpVote) {
            updateWrapper.setSql(
                    "love = love + 1, bad = CASE WHEN " +
                            "bad - 1 < 0 " +
                            "THEN 0 " +
                            "ELSE bad - 1 END"
            );
        } else {
            updateWrapper.setSql(
                    "bad = bad + 1, love = CASE WHEN " +
                            "love - 1 < 0 " +
                            "THEN 0 " +
                            "ELSE love - 1 END"
            );
        }

        critiqueMapper.update(null, updateWrapper);
    }

    /**
     * 单独更新点赞或点踩
     * @param criId    评论id
     * @param column    "love" 点赞 "bad" 点踩
     * @param increase  true 增加 false 减少
     * @param count     更改数量
     * 2024.08.03
     */
    @Override
    public void updateCritique(Integer criId, String column, boolean increase, Integer count) {
        UpdateWrapper<Critique> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", criId);
        if (increase) {
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            // 减少对应值，减少后值必须大于等于0，需要检验
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        critiqueMapper.update(null, updateWrapper);
    }

    /**
     * 举报评论
     * @param id
     * @param userId
     * @param reason
     * @return
     */
    @Override
    public boolean reportCritique(Integer id, Integer userId, String reason) {
        System.out.println("待处理的举报:"+id+"\n用户id:"+userId+"\n举报原因："+reason);
        return true;
    }
}
