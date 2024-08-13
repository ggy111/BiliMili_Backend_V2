package com.bilimili.buaa13.service.critique;


import com.bilimili.buaa13.entity.Critique;
import com.bilimili.buaa13.entity.CritiqueTree;
import com.bilimili.buaa13.entity.ResponseResult;

import java.util.Map;

public interface UserCritiqueService  {


    /**
     * 获取用户点赞和点踩的评论集合
     * @param uid   当前用户
     * @return  点赞和点踩的评论集合
     */
    Map<String, Object> getUserUpVoteAndDownVoteForArticle(Integer uid);

    /**
     * 点赞或点踩某条评论
     * @param uid   当前用户id
     * @param criId    评论id
     * @param isLike true 赞 false 踩
     * @param isCancel true 取消  false 点中
     */
    void setUserUpVoteOrDownVoteForArticle(Integer uid, Integer criId, boolean isLike, boolean isCancel);

    Map<String, Object> getUserUpVoteAndDownVote(Integer postId);

    void setUserUpVoteOrDownVote(Integer postId, Integer criId, boolean isLike, boolean isCancel);
}
