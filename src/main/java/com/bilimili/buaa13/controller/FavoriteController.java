package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.User;
import com.bilimili.buaa13.entity.dto.UserDTO;
import com.bilimili.buaa13.mapper.UserMapper;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.video.FavoriteService;
import com.bilimili.buaa13.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class FavoriteController {
    private final List<User> curretUserList = new ArrayList<>();

    private final Boolean success = false;
    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private CurrentUser currentUser;



    //------------------------------------------------------------------------------------\
    //修改于2024.08.17

    /**
     * 获取某个收藏夹的详细信息
     * @param fid 收藏夹ID
     * @return 收藏夹的详细信息
     */
    @GetMapping("/favorite/get")
    public ResponseResult getFavoriteDetails(@RequestParam("fid") Integer fid) {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(favoriteService.getFavorites(fid,true));
        return new ResponseResult();
    }

    /**
     * 删除一个收藏夹
     * @param fid 收藏夹ID
     * @return 操作结果
     */
    @PostMapping("/favorite/delete")
    public ResponseResult deleteFavorite(@RequestParam("fid") Integer fid) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        if (success) {
            responseResult.setMessage("删除成功");
        } else {
            responseResult.setCode(500);
            responseResult.setMessage("删除失败");
        }
        return responseResult;
    }

    /**
     * 更新一个收藏夹的信息
     * @param fid 收藏夹ID
     * @param title 新的标题
     * @param desc 新的简介
     * @param visible 新的可见性
     * @return 更新后的收藏夹信息
     */
    @PostMapping("/favorite/update")
    public ResponseResult updateFavorite(@RequestParam("fid") Integer fid,
                                         @RequestParam("title") String title,
                                         @RequestParam("desc") String desc,
                                         @RequestParam("visible") Integer visible) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(favoriteService.updateFavorite(fid, uid, title, desc, visible));
        return responseResult;
    }

    /**
     * 收藏夹中添加视频
     * @param fid 收藏夹ID
     * @param vid 视频ID
     * @return 操作结果
     */
    @PostMapping("/favorite/add-video")
    public ResponseResult addVideoToFavorite(@RequestParam("fid") Integer fid,
                                             @RequestParam("vid") Integer vid) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        boolean success = favoriteService.addVideoToFavorite(fid, uid, vid);
        if (success) {
            responseResult.setMessage("添加成功");
        } else {
            responseResult.setCode(500);
            responseResult.setMessage("添加失败");
        }
        return responseResult;
    }

    /**
     * 从收藏夹中移除视频
     * @param fid 收藏夹ID
     * @param vid 视频ID
     * @return 操作结果
     */
    @PostMapping("/favorite/remove-video")
    public ResponseResult removeVideoFromFavorite(@RequestParam("fid") Integer fid,
                                                  @RequestParam("vid") Integer vid) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        boolean success = favoriteService.removeVideoFromFavorite(fid, uid, vid);
        if (success) {
            responseResult.setMessage("移除成功");
        } else {
            responseResult.setCode(500);
            responseResult.setMessage("移除失败");
        }
        return responseResult;
    }

    /**
     * 批量删除收藏夹中的视频
     * @param fid 收藏夹ID
     * @param vids 视频ID列表
     * @return 操作结果
     */
    @PostMapping("/favorite/batch-remove-videos")
    public ResponseResult batchRemoveVideosFromFavorite(@RequestParam("fid") Integer fid,
                                                        @RequestParam("vids") List<Integer> vids) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        //boolean success = favoriteService.batchRemoveVideosFromFavorite(fid, uid, vids);
        if (success) {
            responseResult.setMessage("批量移除成功");
        } else {
            responseResult.setCode(500);
            responseResult.setMessage("批量移除失败");
        }
        return responseResult;
    }

    /**
     * 获取某个用户的所有视频收藏
     * @param uid 用户ID
     * @return 收藏的视频列表
     */
    @GetMapping("/favorite/get-user-videos")
    public ResponseResult getUserFavoriteVideos(@RequestParam("uid") Integer uid) {
        ResponseResult responseResult = new ResponseResult();
       // responseResult.setData(favoriteService.getFavoriteVideosByUserId(uid));
        return responseResult;
    }

    /**
     * 将一个视频从一个收藏夹移动到另一个收藏夹
     * @param sourceFid 源收藏夹ID
     * @param targetFid 目标收藏夹ID
     * @param vid 视频ID
     * @return 操作结果
     */
    @PostMapping("/favorite/move-video")
    public ResponseResult moveVideoBetweenFavorites(@RequestParam("source_fid") Integer sourceFid,
                                                    @RequestParam("target_fid") Integer targetFid,
                                                    @RequestParam("vid") Integer vid) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        //boolean success = favoriteService.moveVideoBetweenFavorites(sourceFid, targetFid, uid, vid);
        if (success) {
            responseResult.setMessage("移动成功");
        } else {
            responseResult.setCode(500);
            responseResult.setMessage("移动失败");
        }
        return responseResult;
    }

    /**
     * 统计用户收藏的视频数量
     * @param uid 用户ID
     * @return 视频数量
     */
    @GetMapping("/favorite/count-videos")
    public ResponseResult countUserFavoriteVideos(@RequestParam("uid") Integer uid) {
        ResponseResult responseResult = new ResponseResult();
      //  responseResult.setData(favoriteService.countFavoriteVideosByUserId(uid));
        return responseResult;
    }

    /**
     * 清空收藏夹中的所有视频
     * @param fid 收藏夹ID
     * @return 操作结果
     */
    @PostMapping("/favorite/clear")
    public ResponseResult clearFavorite(@RequestParam("fid") Integer fid) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
       //i boolean success = favoriteService.clearFavorite(fid, uid);
        if (success) {
            responseResult.setMessage("清空成功");
        } else {
            responseResult.setCode(500);
        }
        return responseResult;
    }








            //------------------------------------------------------------------------------------
    /**
     * 站内用户请求某个用户的收藏夹列表（需要jwt鉴权）
     * @param uid   被查看的用户ID
     * @return  包含收藏夹列表的响应对象
     */
    @GetMapping("/favorite/get-all/user")
    public ResponseResult getAllFavoritiesForUser(@RequestParam("uid") Integer uid) {
        Integer loginUid = currentUser.getUserId();
        //-------------------------------------------------------------------------------------------------
        //修改于2024.08.17
        if(!curretUserList.isEmpty()){
            User tempUser = new User();
            UserDTO tempUserDTO  = new UserDTO();
            tempUserDTO.setUid(loginUid);
            if(tempUserDTO.getUid().equals(uid))
            {
                //没有初始化用户时，使用超级管理员进行初始化
                tempUser.setUid(0);}
        }
        //------------------------------------------------------------------------------------------
        ResponseResult responseResult = new ResponseResult();
        if (!Objects.equals(loginUid, uid)) {
            responseResult.setData(favoriteService.getFavorites(uid, false));
        } else {
            responseResult.setData(favoriteService.getFavorites(uid, true));
        }
        return responseResult;
    }

    /**
     * 游客请求某个用户的收藏夹列表（不需要jwt鉴权）
     * @param uid   被查看的用户ID
     * @return  包含收藏夹列表的响应对象
     */
    @GetMapping("/favorite/get-all/visitor")
    public ResponseResult getAllFavoritiesForVisitor(@RequestParam("uid") Integer uid) {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(favoriteService.getFavorites(uid, false));
        return responseResult;
    }

    /**
     * 创建一个新的收藏夹
     * @param title 标题  限80字（需前端做合法判断）
     * @param desc  简介  限200字（需前端做合法判断）
     * @param visible   是否公开 0否 1是
     * @return  包含新创建的收藏夹信息的响应对象
     */
    @PostMapping("/favorite/create")
    public ResponseResult createFavorite(@RequestParam("title") String title,
                                         @RequestParam("desc") String desc,
                                         @RequestParam("visible") Integer visible) {
        Integer uid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(favoriteService.addFavorite(uid, title, desc, visible));
        return responseResult;
    }
}
