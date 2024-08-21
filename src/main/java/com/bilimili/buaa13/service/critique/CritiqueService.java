package com.bilimili.buaa13.service.critique;

import com.bilimili.buaa13.entity.Critique;
import com.bilimili.buaa13.entity.CritiqueTree;
import com.bilimili.buaa13.entity.ResponseResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CritiqueService {
    //----------------------------------------------------------------------------
    //基于原来思路的额外修改
    @Transactional
    ResponseResult deleteCritiqueFromArticle(Integer criId, Integer postId, boolean isAdmin);

    /**
     * 根据文章id获取评论
     * @param aid 文章id
     * @param offset 已获取的评论树数量
     * @param sortType 排序方式
     * @return 评论树数组
     */
    List<CritiqueTree> getCritiqueTreeByAid(Integer aid, Long offset, Integer sortType);

    /**
     * 发送评论，字数不得大于2000或为空
     * @param aid   文章id
     * @param postId   发布者postId
     * @param rootId    楼层id（根评论id）
     * @param parentId  被回复的评论id
     * @param acceptId  被回复用户postId
     * @param content   评论内容
     * @return  true 发送成功 false 发送失败
     */
    CritiqueTree sendCritique(Integer aid, Integer postId, Integer rootId, Integer parentId, Integer acceptId, String content);


    /**
     * 删除评论
     * @param criId    评论id
     * @param postId   当前用户id
     * @param isAdmin   是否是管理员
     * @return  响应对象
     */
    ResponseResult deleteCritique(Integer criId, Integer postId, boolean isAdmin);


    /**
     * @param rootId 根级节点的评论 criId, 即楼层 criId
     * @return 1. 根据 redis 查找出回复该评论的子评论 criId 列表
     * 2. 根据 criId 查询出所有评论的详细信息
     */
    List<Critique> getChildCritiquesByRootId(Integer rootId, Long start, Long end);

    /**
     * 根据文章 aid 获取根评论列表，一次查 10 条
     * @param aid 文章 criId
     * @param offset 偏移量，已经获取到的根评论数量
     * @param sortType 1:按热度排序 2:按时间排序
     * @return List<Critique>
     */
    List<Critique> getRootCritiquesByAid(Integer aid, Long offset, Integer sortType);

    /**
     * 获取更多回复评论
     * @param criId 根评论id
     * @return  包含全部回复评论的评论树
     */
    CritiqueTree getMoreCritiquesById(Integer criId);

    /*--------------------评论点赞点踩相关-----------------------*/
    /**
     * 同时相对更新点赞和点踩
     * 用于原本点踩了，现在直接点赞，一次改完。
     * @param criId    评论id
     * @param addUpVote   true 点赞 false 点踩
     */
    void updateLikeAndDisLike(Integer criId, boolean addUpVote);

    /**
     * 单独更新点赞或点踩
     * @param criId    评论id
     * @param column    "love" 点赞 "bad" 点踩
     * @param increase  true 增加 false 减少
     * @param count     更改数量
     */
    void updateCritique(Integer criId, String column, boolean increase, Integer count);


    /**
     * 举报评论
     * @param id
     * @param userId
     * @param reason
     * @return
     */
    boolean reportCritique(Integer id, Integer userId, String reason);
}
