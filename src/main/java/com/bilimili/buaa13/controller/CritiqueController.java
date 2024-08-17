package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.CritiqueTree;
import com.bilimili.buaa13.service.critique.CritiqueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bilimili.buaa13.tools.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;


import java.util.Collections;
import com.bilimili.buaa13.entity.ResponseResult;
import java.util.Map;
import org.springframework.web.bind.annotation.RestController;
import com.bilimili.buaa13.service.utils.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
public class CritiqueController {
    @Autowired
    private CritiqueService commentService;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private RedisTool redisTool;

    /**
     * 获取评论树列表，每次查十条
     * @param aid   对应视频ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param sortType  排序类型 1 按热度排序 2 按时间排序
     * @return  评论树列表
     */
    @GetMapping("/comment/get")
    public ResponseResult getCritiqueTreeByAid(@RequestParam("aid") Integer aid,
                                              @RequestParam("offset") Long offset,
                                              @RequestParam("sortType") Integer sortType) {
        ResponseResult responseResult = new ResponseResult();
        long count = redisTool.getZSetNumber("comment_video:" + aid);
        Map<String, Object> map = new HashMap<>();
        if (offset >= count) {
            // 表示前端已经获取到全部根评论了，没必要继续
            map.put("more", false);
            map.put("comments", Collections.emptyList());
        } else if (offset + 10 >= count){
            // 表示这次查询会查完全部根评论
            map.put("more", false);
            map.put("comments", commentService.getCritiqueTreeByAid(aid, offset, sortType));
        } else {
            // 表示这次查的只是冰山一角，还有很多评论没查到
            map.put("more", true);
            map.put("comments", commentService.getCritiqueTreeByAid(aid, offset, sortType));
        }
        responseResult.setData(map);
        return responseResult;
    }

    /**
     * 展开更多回复评论
     * @param id 根评论id
     * @return 完整的一棵包含全部评论的评论树
     */
    @GetMapping("/comment/reply/get-more")
    public CritiqueTree getMoreCritiqueById(@RequestParam("id") Integer id) {
        return commentService.getMoreCritiquesById(id);
    }

    /**
     * 发表评论
     * @param aid   视频id
     * @param rootId    根评论id
     * @param parentId  被回复评论id
     * @param acceptId  被回复者postId
     * @param content   评论内容
     * @return  响应对象
     */
    @PostMapping("/comment/add")
    public ResponseResult addCritique(
            @RequestParam("aid") Integer aid,
            @RequestParam("root_id") Integer rootId,
            @RequestParam("parent_id") Integer parentId,
            @RequestParam("to_user_id") Integer acceptId,
            @RequestParam("content") String content ) {
        Integer postId = currentUser.getUserId();

        ResponseResult responseResult = new ResponseResult();
        CritiqueTree commentTree = commentService.sendCritique(aid, postId, rootId, parentId, acceptId, content);
        if (commentTree == null) {
            responseResult.setCode(500);
            responseResult.setMessage("发送失败！");
        }
        responseResult.setData(commentTree);
        return responseResult;
    }

    /**
     * 删除评论
     * @param id 评论id
     * @return  响应对象
     */
    @PostMapping("/comment/delete")
    public ResponseResult delCritique(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return commentService.deleteCritique(id, loginUid, currentUser.isAdmin());
    }
}
