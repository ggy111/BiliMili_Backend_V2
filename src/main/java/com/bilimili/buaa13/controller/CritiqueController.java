package com.bilimili.buaa13.controller;


import com.bilimili.buaa13.entity.CritiqueTree;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.critique.CritiqueService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.tools.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CritiqueController {

    @Autowired
    private CritiqueService critiqueService;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RedisTool redisTool;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取评论树列表，每次查十条
     * @param aid 视频ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param sortType 排序类型 1 按热度排序 2 按时间排序
     * @return 评论树列表
     */
    @GetMapping("/critique/get")
    public ResponseResult getCritiqueTreeByAid(@RequestParam("aid") Integer aid,
                                               @RequestParam("offset") Long offset,
                                               @RequestParam("sortType") Integer sortType) {
        ResponseResult responseResult = new ResponseResult();
        Long count = redisTemplate.opsForZSet().zCard("critique_aid:" + aid);
        if(count == null){return responseResult;}
        Map<String, Object> map = new HashMap<>();
        if (offset >= count) {
            // 前端已获取全部根评论
            map.put("more", false);
            map.put("critiques", Collections.emptyList());
        } else if (offset + 10 >= count) {
            // 本次查询会查完 全部 根评论
            map.put("more", false);
            map.put("critiques", critiqueService.getCritiqueTreeByAid(aid, offset, sortType));
        } else {
            // 还有更多评论未查询
            map.put("more", true);
            map.put("critiques", critiqueService.getCritiqueTreeByAid(aid, offset, sortType));
        }
        responseResult.setData(map);
        return responseResult;
    }

    /**
     * 展开更多回复评论
     * @param id 根评论id
     * @return 完整的一棵包含全部评论的评论树
     */
    @GetMapping("/critique/reply/get-more")
    public CritiqueTree getMoreCritiqueById(@RequestParam("id") Integer id) {
        return critiqueService.getMoreCritiquesById(id);
    }

    /**
     * 发表评论
     * @param aid 视频id
     * @param rootId 根评论id
     * @param parentId 被回复评论id
     * @param acceptId 被回复者postId
     * @param content 评论内容
     * @return 响应对象
     */
    @PostMapping("/critique/add")
    public ResponseResult addCritique(
            @RequestParam("aid") Integer aid,
            @RequestParam("root_id") Integer rootId,
            @RequestParam("parent_id") Integer parentId,
            @RequestParam("to_user_id") Integer acceptId,
            @RequestParam("content") String content) {
        Integer postId = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        CritiqueTree critiqueTree = critiqueService.sendCritique(aid, postId, rootId, parentId, acceptId, content);
        if (critiqueTree == null) {
            responseResult.setCode(500);
            responseResult.setMessage("发送失败！");
        } else {
            responseResult.setData(critiqueTree);
        }
        return responseResult;
    }

    /**
     * 删除评论
     * @param id 评论id
     * @return 响应对象
     */
    @PostMapping("/critique/delete")
    public ResponseResult delCritique(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return critiqueService.deleteCritique(id, loginUid, currentUser.isAdmin());
    }
}
