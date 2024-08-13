package com.bilimili.buaa13.service.impl.critique;

import com.bilimili.buaa13.service.critique.UserCritiqueService;

package com.bilimili.buaa13.service.impl.critique;

import com.bilimili.buaa13.service.critique.UserCritiqueService;
import com.bilimili.buaa13.service.critique.CritiqueService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.bilimili.buaa13.utils.RedisUtil;

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class UserCritiqueServiceImpl implements UserCritiqueService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CritiqueService critiqueService;


    /**
     *
     *@Override
     * public void setUserUpVoteOrDownVote(Integer postId, Integer criId, boolean isLike, boolean isCancel) {
     *     boolean hasLiked = redisUtil.isMember("upVote:" + postId, criId);
     *     boolean hasDisliked = redisUtil.isMember("downVote:" + postId, criId);
     *
     *     // 如果同时存在点赞和点踩的情况（理论上不应该发生）
     *     if (hasLiked && hasDisliked) {
     *         throw new IllegalStateException("系统错误：同时存在点赞和点踩记录");
     *     }
     *
     *     if (isCancel) {
     *         if (isLike && hasLiked) {
     *             // 取消点赞
     *             redisUtil.delMember("upVote:" + postId, criId);
     *             critiqueService.updateCritique(criId, "up_vote", false, 1);
     *         } else if (!isLike && hasDisliked) {
     *             // 取消点踩
     *             redisUtil.delMember("downVote:" + postId, criId);
     *             critiqueService.updateCritique(criId, "down_vote", false, 1);
     *         }
     *     } else {
     *         if (isLike) {
     *             if (hasDisliked) {
     *                 // 取消点踩并点赞
     *                 redisUtil.delMember("downVote:" + postId, criId);
     *                 critiqueService.updateCritique(criId, "down_vote", false, 1);
     *             }
     *             if (!hasLiked) {
     *                 // 添加点赞
     *                 redisUtil.addMember("upVote:" + postId, criId);
     *                 critiqueService.updateCritique(criId, "up_vote", true, 1);
     *             }
     *         } else {
     *             if (hasLiked) {
     *                 // 取消点赞并点踩
     *                 redisUtil.delMember("upVote:" + postId, criId);
     *                 critiqueService.updateCritique(criId, "up_vote", false, 1);
     *             }
     *             if (!hasDisliked) {
     *                 // 添加点踩
     *                 redisUtil.addMember("downVote:" + postId, criId);
     *                 critiqueService.updateCritique(criId, "down_vote", true, 1);
     *             }
     *         }
     *     }
     * }
     * **/






    /**
     * 获取用户点赞和点踩的评论集合
     * @param postId   当前用户
     * @return  点赞和点踩的评论集合
     */
    @Override
    public Map<String, Object> getUserUpVoteAndDownVote(Integer postId) {
        Map<String, Object> map = new HashMap<>();
        Set<Object> userLike = redisUtil.getMembers("upVote:" + postId);
        Set<Object> userDislike = redisUtil.getMembers("downVote:" + postId);
        map.put("userLike", userLike==null?new ArrayList<>():userLike);
        map.put("userDislike", userDislike==null?new ArrayList<>():userDislike);
        //注释Redis
        //注释异步线程
        /*// 获取用户点赞列表，并放入map中
        CompletableFuture<Void> userLikeFuture = CompletableFuture.runAsync(() -> {

            if (userLike == null) {
                map.put("userLike", Collections.emptySet());
            } else{
                map.put("userLike", userLike);
            }
        }, taskExecutor);
        // 获取用户点踩列表，并放入map中
        CompletableFuture<Void> userDislikeFuture = CompletableFuture.runAsync(() -> {

            map.put("userDislike", userDislike);
            if (userDislike == null) {
                map.put("userDislike", Collections.emptySet());
            } else {
                map.put("userDislike", userDislike);
            }
        }, taskExecutor);

        userDislikeFuture.join();
        userLikeFuture.join();*/

        return map;
    }

    /**
     * 点赞或点踩某条评论
     * @param postId   当前用户id
     * @param criId    评论id
     * @param isLike true 赞 false 踩
     * @param isCancel true 取消 false 点中
     */
    @Override
    public void setUserUpVoteOrDownVote(Integer postId, Integer criId, boolean isLike, boolean isCancel) {
        Boolean likeExist = redisUtil.isMember("upVote:" + postId, criId);
        Boolean dislikeExist = redisUtil.isMember("downVote:" + postId, criId);
        //理论上，likeExist和disLikeExist不能同时存在,所以相加不等于2
        if(boolChangeBinary(likeExist) + boolChangeBinary(dislikeExist) == 1 ){
            //以likeExist为基准，要么like要么dislike
            int judgeNumber = (boolChangeBinary(likeExist)<<2) | (boolChangeBinary(isLike)<< 1) | boolChangeBinary(isCancel);
            if(judgeNumber == 6){
                //点赞，并且不是取消，原本已经点赞
                return;
            }
            else if(judgeNumber == 7){
                //已经点赞，现在需要取消
                // 移除点赞记录
                redisUtil.delMember("upVote:" + postId, criId);
                // 更新评论点赞数
                critiqueService.updateCritique(criId, "up_vote", false, 1);
            }
            else if(judgeNumber == 5){
                //以前点了赞，现在需要取消踩。不需要取消
                return;
            }
            else if(judgeNumber == 4){
                //以前点了赞，现在需要点踩
                // 更新用户点踩记录
                redisUtil.addMember("downVote:" + postId, criId);
                // 原本点了赞，要取消赞
                redisUtil.delMember("upVote:" + postId, criId);
                // 更新评论点赞点踩的记录
                critiqueService.updateLikeAndDisLike(criId, false);
            }
            else if(judgeNumber == 3){
                //原本点了踩，现在需要取消点赞，直接返回
                return;
            }
            else if(judgeNumber == 2){
                //原本点了踩，现在需要点赞
                // 添加点赞记录
                redisUtil.addMember("upVote:" + postId, criId);
                // 原本点了踩，就要取消踩
                // 1.redis中删除点踩记录
                redisUtil.delMember("downVote:" + postId, criId);
                // 2. 数据库中更改评论的点赞点踩数
                critiqueService.updateLikeAndDisLike(criId, true);
            }
            else if(judgeNumber == 1){
                //原本点了踩，现在需要取消踩
                // 取消用户点踩记录
                redisUtil.delMember("downVote:" + postId, criId);
                // 更新评论点踩数量
                critiqueService.updateCritique(criId, "down_vote", false, 1);
            }
            else if (judgeNumber == 0){
                //原本点了踩，现在还要点踩，直接返回
                return;
            }
            else {
                //不应当出现其他情况，抛出报错信息
                System.out.println("点赞相关数值判断出现错误，请检查函数setUserUpVoteOrDownVote");
                return;
            }
        }
        else if(boolChangeBinary(likeExist) + boolChangeBinary(dislikeExist) == 0 ){
            //以前没点过赞，没点过踩，只用判断后两位即可
            int judgeNumber = (boolChangeBinary(isLike)<< 1) | boolChangeBinary(isCancel);
            switch (judgeNumber) {
                case 0:
                    //选中点踩
                    // 原本没有点赞，直接点踩，更新评论点踩数量
                    // 添加点踩记录
                    redisUtil.addMember("downVote:" + postId, criId);
                    critiqueService.updateCritique(criId, "down_vote", true, 1);
                    break;
                case 1:
                    //取消点踩,以前没有点踩，直接过
                    break;
                case 2:
                    //点赞,但是原本没有点赞
                    // 添加点赞记录
                    redisUtil.addMember("upVote:" + postId, criId);
                    // 原来没点踩，只需要点赞, 这里只更新评论的点赞数
                    critiqueService.updateCritique(criId, "up_vote", true, 1);
                    break;
                case 3:
                    //取消点赞，以前没有点赞，直接过
                    break;
            }
        }
    }

    /**
     * 转换
     * @param boolNumber bool变量
     * @return 二进制数
     */
    private Integer boolChangeBinary(boolean boolNumber){
        return boolNumber?1:0;
    }




}
