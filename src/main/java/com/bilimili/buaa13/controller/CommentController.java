package com.bilimili.buaa13.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.message.ChatDetailedService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.CommentTree;
import com.bilimili.buaa13.service.comment.CommentService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.utils.RedisUtil;

import java.util.*;


/**
 * @RestController
 * public class CommentController {
 *     @Autowired
 *     private CritiqueService commentService;
 *     @Autowired
 *     private CurrentUser currentUser;
 *     @Autowired
 *     private RedisUtil redisUtil;
 * **/
//这里需要做整体修改


    /**
     * 获取评论树列表，每次查十条
     * @param aid   对应视频ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param sortType  排序类型 1 按热度排序 2 按时间排序
     * @return  评论树列表
     */
    /**
    @GetMapping("/comment/get")
    public ResponseResult getCritiqueTreeByAid(@RequestParam("aid") Integer aid,
                                              @RequestParam("offset") Long offset,
                                              @RequestParam("sortType") Integer sortType) {
        ResponseResult responseResult = new ResponseResult();
        long count = redisUtil.zCard("comment_aideo:" + aid);
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
    }**/

    /**
     * 展开更多回复评论
     * @param id 根评论id
     * @return 完整的一棵包含全部评论的评论树
     */
   /** @GetMapping("/comment/reply/get-more")
    public CritiqueTree getMoreCritiqueById(@RequestParam("id") Integer id) {
        return commentService.getMoreCritiquesById(id);
    }**/

    /**
     * 发表评论
     * @param aid   视频id
     * @param rootId    根评论id
     * @param parentId  被回复评论id
     * @param acceptId  被回复者postId
     * @param content   评论内容
     * @return  响应对象
     */
    /**@PostMapping("/comment/add")
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
    }**/

    /**
     * 删除评论
     * @param id 评论id
     * @return  响应对象
     */
    /**@PostMapping("/comment/delete")
    public ResponseResult delCritique(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return commentService.deleteCritique(id, loginUid, currentUser.isAdmin());
    }
}**/





    //该函数未被修改
@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取评论树列表，每次查十条
     * @param vid   对应视频ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param type  排序类型 1 按热度排序 2 按时间排序
     * @return  评论树列表
     */
    @GetMapping("/comment/get")
    public ResponseResult getCommentTreeByVid(@RequestParam("vid") Integer vid,
                                              @RequestParam("offset") Long offset,
                                              @RequestParam("type") Integer type) {
        ResponseResult customResponse = new ResponseResult();
        long count = redisUtil.zCard("comment_video:" + vid);
        Map<String, Object> map = new HashMap<>();
        if (offset >= count) {
            // 表示前端已经获取到全部根评论了，没必要继续
            map.put("more", false);
            map.put("comments", Collections.emptyList());
        } else if (offset + 10 >= count){
            // 表示这次查询会查完全部根评论
            map.put("more", false);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, type));
        } else {
            // 表示这次查的只是冰山一角，还有很多评论没查到
            map.put("more", true);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, type));
        }
        customResponse.setData(map);
        return customResponse;
    }

    /**
     * 展开更多回复评论
     * @param id 根评论id
     * @return 完整的一棵包含全部评论的评论树
     */
    @GetMapping("/comment/reply/get-more")
    public CommentTree getMoreCommentById(@RequestParam("id") Integer id) {
        return commentService.getMoreCommentsById(id);
    }

    /**
     * 发表评论
     * @param vid   视频id
     * @param rootId    根评论id
     * @param parentId  被回复评论id
     * @param toUserId  被回复者uid
     * @param content   评论内容
     * @return  响应对象
     */
    @PostMapping("/comment/add")
    public ResponseResult addComment(
            @RequestParam("vid") Integer vid,
            @RequestParam("root_id") Integer rootId,
            @RequestParam("parent_id") Integer parentId,
            @RequestParam("to_user_id") Integer toUserId,
            @RequestParam("content") String content ) {
        Integer uid = currentUser.getUserId();

        ResponseResult customResponse = new ResponseResult();
        CommentTree commentTree = commentService.sendComment(vid, uid, rootId, parentId, toUserId, content);
        if (commentTree == null) {
            customResponse.setCode(500);
            customResponse.setMessage("发送失败！");
        }
        customResponse.setData(commentTree);
        return customResponse;
    }

    /**
     * 删除评论
     * @param id 评论id
     * @return  响应对象
     */
    @PostMapping("/comment/delete")
    public ResponseResult delComment(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return commentService.deleteComment(id, loginUid, currentUser.isAdmin());
    }
}