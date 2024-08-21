package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.CommentTree;
import com.bilimili.buaa13.entity.CritiqueTree;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.comment.CommentService;
import com.bilimili.buaa13.service.critique.CritiqueService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private CurrentUser currentUser;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private CritiqueService critiqueService;

    /**
     * 获取评论树列表，每次查十条
     * @param vid   对应视频ID
     * @param offset 分页偏移量（已经获取到的评论树的数量）
     * @param sortType  排序类型 1 按热度排序 2 按时间排序
     * @return  评论树列表
     */
    @GetMapping("/comment/get")
    public ResponseResult getCritiqueTreeByAid(@RequestParam("vid") Integer vid,
                                              @RequestParam("offset") Long offset,
                                              @RequestParam("type") Integer sortType) {
        ResponseResult responseResult = new ResponseResult();
        Long count = redisTemplate.opsForZSet().zCard("comment_video:" + vid);
        //System.out.println("getCritiqueTreeByAid "+count);
        if (count == null) {return responseResult;}
        Map<String, Object> map = new HashMap<>();
        if (offset >= count) {
            // 表示前端已经获取到全部根评论了，没必要继续
            map.put("more", false);
            map.put("comments", Collections.emptyList());
        } else if (offset + 10 >= count){
            // 表示这次查询会查完全部根评论
            map.put("more", false);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, sortType));
        } else {
            // 表示这次查的只是冰山一角，还有很多评论没查到
            map.put("more", true);
            map.put("comments", commentService.getCommentTreeByVid(vid, offset, sortType));
        }
        responseResult.setData(map);
        //System.out.println("getCritiqueTreeByAid map: "+map);
        return responseResult;
    }

    /**
     * 展开更多回复评论
     * @param id 根评论id
     * @return 完整的一棵包含全部评论的评论树
     */
    @GetMapping("/comment/reply/get-more")
    public CommentTree getMoreCritiqueById(@RequestParam("id") Integer id) {
        return commentService.getMoreCommentsById(id);
    }

    /**
     * 发表评论
     * @param vid   视频id
     * @param rootId    根评论id
     * @param parentId  被回复评论id
     * @param acceptId  被回复者postId
     * @param content   评论内容
     * @return  响应对象
     */
    @PostMapping("/comment/add")
    public ResponseResult addCritique(
            @RequestParam("vid") Integer vid,
            @RequestParam("root_id") Integer rootId,
            @RequestParam("parent_id") Integer parentId,
            @RequestParam("to_user_id") Integer acceptId,
            @RequestParam("content") String content ) {
        return getCritiqueResponseResult(vid, rootId, parentId, acceptId, content, currentUser, commentService);
    }

    @NotNull
    private ResponseResult getCritiqueResponseResult(@RequestParam("vid") Integer vid, @RequestParam("root_id") Integer rootId, @RequestParam("parent_id") Integer parentId, @RequestParam("to_user_id") Integer toUserId, @RequestParam("content") String content, CurrentUser currentUser, CommentService commentService) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        CommentTree commentTree = commentService.sendComment(vid, uid, rootId, parentId, toUserId, content);
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
        return commentService.deleteComment(id, loginUid, currentUser.isAdmin());
    }

    /**
     * 获取特定用户的所有评论
     * @param userId 用户id
     * @param page 页码
     * @param size 每页数量
     * @return 包含评论列表的响应对象
     */
    @GetMapping("/comment/user/get")
    public ResponseResult getCommentsByUser(@RequestParam("user_id") Integer userId,
                                            @RequestParam("page") Long page,
                                            @RequestParam("size") Integer size) {
        List<CritiqueTree> comments = critiqueService.getCritiqueTreeByAid(userId, page, size);
        ResponseResult responseResult = new ResponseResult();
        if (comments.isEmpty()) {
            responseResult.setCode(404);
            responseResult.setMessage("该用户没有评论。");
        } else {
            responseResult.setData(comments);
        }
        return responseResult;
    }

    /**
     * 点赞评论
     * @param id 评论id
     * @return 响应对象
     */
    @PostMapping("/comment/like")
    public ResponseResult likeCritique(@RequestParam("id") Integer id) {
        Integer userId = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        Boolean success = true;
        critiqueService.updateCritique(id, userId.toString(),success,1);
        if (Boolean.TRUE.equals(success)) {
            responseResult.setMessage("点赞成功！");
        } else {
            responseResult.setCode(500);
            responseResult.setMessage("点赞失败！");
        }
        return responseResult;
    }

    /**
     * 举报评论
     * @param id 评论id
     * @param reason 举报原因
     * @return 响应对象
     */
    @PostMapping("/comment/report")
    public ResponseResult reportCritique(@RequestParam("id") Integer id,
                                         @RequestParam("reason") String reason) {
        Integer userId = currentUser.getUserId();
        boolean success = critiqueService.reportCritique(id, userId, reason);

        ResponseResult responseResult = new ResponseResult();
        if (success) {
            responseResult.setMessage("举报成功！");
        } else {
            responseResult.setCode(500);
            responseResult.setMessage("举报失败！");
        }
        return responseResult;
    }
}
