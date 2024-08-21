package com.bilimili.buaa13.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bilimili.buaa13.entity.Follow;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.User;
import com.bilimili.buaa13.entity.VideoStatus;
import com.bilimili.buaa13.entity.dto.UserDTO;
import com.bilimili.buaa13.mapper.FollowMapper;
import com.bilimili.buaa13.mapper.UserMapper;
import com.bilimili.buaa13.service.user.UserAccountService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.tools.ESTool;
import com.bilimili.buaa13.tools.OssTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private VideoStatusService videoStatusService;

    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private RedisTool redisTool;

    @Autowired
    private ESTool esTool;

    @Autowired
    private OssTool ossTool;

    @Value("${oss.bucketUrl}")
    private String OSS_BUCKET_URL;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据uid查询用户信息
     * @param id 用户ID
     * @return 用户可见信息实体类 UserDTO
     */
    @Override
    public UserDTO getUserByUId(Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;    // 如果uid不存在则返回空
        }
        UserDTO userDTO = new UserDTO();
        if (user.getState() == 2) {
            userDTO = userAccountService.setSignOutUserDTO(user);
            return userDTO;
        }
        userDTO = userAccountService.setUserDTO(user);
        userDTO.setFollowsCount(0);
        userDTO.setFansCount(0);
        //获取用户对应的视频列表
        //"user_video_upload:" + user.getUid()是完整的键值，可以取出内容。
        //1注释Redis
        Set<Object> set = redisTool.reverseRange("user_video_upload:" + user.getUid(), 0L, -1L);
        if (set == null || set.isEmpty()) {
            userDTO.setVideoCount(0);
            userDTO.setLoveCount(0);
            userDTO.setPlayCount(0);
        }
        else{
            // 并发执行每个视频数据统计的查询任务
            //并行流方式遍历列表
            List<VideoStatus> list = set.stream().parallel()
                .map(vid -> videoStatusService.getStatusByVideoId((Integer) vid))
                .toList();
            List<Object> listSet = new ArrayList<>(set);
            list = new ArrayList<>();

            int setSize = listSet.size();
            for(int i=0;i< setSize;++i){
                int vid = (int)listSet.get(i);
                VideoStatus videoStatus = videoStatusService.getStatusByVideoId(vid);
                list.add(videoStatus);
            }
            //遍历查找用户播放，点赞总数据
            int video = list.size(), love = 0, play = 0;
            userDTO.setVideoCount(video);
            while(video>0){
                video--;
                VideoStatus videoStatus = list.get(video);
                love = love + videoStatus.getGood();
                play = play + videoStatus.getPlay();
            }

            userDTO.setLoveCount(love);
            userDTO.setPlayCount(play);

        }
        QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("uidFollow", user.getUid());
        Integer fans = followMapper.getUidFansByUid(user.getUid()).size();
        userDTO.setFansCount(fans);
        Integer follow = followMapper.getUidFollowByUid(user.getUid()).size();
        userDTO.setFollowsCount(follow);
        return userDTO;
    }

    @Override
    public List<UserDTO> getUserByUIdList(List<Integer> list) {
        if (list.isEmpty()) return Collections.emptyList();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("uid", list).ne("state", 2);
        List<User> users = userMapper.selectList(queryWrapper);
        if (users.isEmpty()) return Collections.emptyList();
        List<UserDTO> userDTOList = new ArrayList<>();
        int listSize = list.size();
        for(int i=0;i<listSize;++i){
            Integer uid = list.get(i);
            User user = null;
            for(int j=0;j<users.size();++j){
                User user1 = users.get(j);
                if(Objects.equals(user1.getUid(), uid)){
                    user = user1;
                    break;
                }
            }
            if(user == null){continue;}
            UserDTO userDTO = new UserDTO(
                    user.getUid(),
                    user.getNickname(),
                    user.getHeadPortrait(),
                    user.getBackground(),
                    user.getGender(),
                    user.getDescription(),
                    user.getExperience(),
                    user.getCoin(),
                    user.getState(),
                    0,0,0,0,0
            );
            Set<Object> set = redisTool.reverseRange("user_video_upload:" + user.getUid(), 0L, -1L);

            if (set == null || set.isEmpty()) {
                userDTOList.add(userDTO);
                continue;
            }

            List<Object> listSet = new ArrayList<>(set);
            List<VideoStatus> listVideoStats = new ArrayList<>();

            int setSize = listSet.size();
            for(int j=0;j< setSize;++j){
                int vid = (int)listSet.get(j);
                VideoStatus videoStatus = videoStatusService.getStatusByVideoId(vid);
                listVideoStats.add(videoStatus);
            }
            //遍历查找用户播放，点赞总数据
            int video = listVideoStats.size(), love = 0, play = 0;
            userDTO.setVideoCount(video);
            while(video>0){
                video--;
                VideoStatus videoStatus = listVideoStats.get(video);
                love = love + videoStatus.getGood();
                play = play + videoStatus.getPlay();
            }
            userDTO.setLoveCount(love);
            userDTO.setPlayCount(play);

            userDTOList.add(userDTO);
        }
        return userDTOList;
    }

    @Override
    @Transactional
    public ResponseResult updateUserInformation(Integer uid, String nickname, String desc, Integer gender) throws IOException {
        ResponseResult responseResult = new ResponseResult();
        if (nickname == null || nickname.trim().isEmpty()) {
            responseResult.setCode(500);
            responseResult.setMessage("昵称不能为空");
            return responseResult;
        }
        if (nickname.length() > 24 || desc.length() > 100) {
            responseResult.setCode(500);
            responseResult.setMessage("输入字符过长");
            return responseResult;
        }
        if (Objects.equals(nickname, "账号已注销")) {
            responseResult.setCode(500);
            responseResult.setMessage("昵称非法");
            return responseResult;
        }
        // 查重
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nickname", nickname).ne("uid", uid);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            responseResult.setCode(500);
            responseResult.setMessage("该昵称已被其他用户占用");
            return responseResult;
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid)
                .set("nickname", nickname)
                .set("description", desc)
                .set("gender", gender);
        userMapper.update(null, updateWrapper);
        User new_user = new User();
        new_user.setUid(uid);
        new_user.setNickname(nickname);
        esTool.updateUser(new_user);
        //1注释Redis
        redisTool.deleteValue("user:" + uid);
        return responseResult;
    }

    @Override
    public ResponseResult updateUserHeadPortrait(Integer uid, MultipartFile file) throws IOException {
        ResponseResult responseResult = new ResponseResult();
        // 保存封面到OSS，返回URL
        String headPortrait_url = ossTool.uploadImage(file, "headPortrait");
        // 查旧的头像地址
        User user = userMapper.selectById(uid);
        // 先更新数据库
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).set("head_portrait", headPortrait_url);
        userMapper.update(null, updateWrapper);
        CompletableFuture.runAsync(() -> {
            //1注释Redis
            redisTool.deleteValue("user:" + uid);  // 删除redis缓存
            // 如果就头像不是初始头像就去删除OSS的源文件
            if (user.getHeadPortrait().startsWith(OSS_BUCKET_URL)) {
                String filename = user.getHeadPortrait().substring(OSS_BUCKET_URL.length());
                ossTool.deleteFiles(filename);
            }
        }, taskExecutor);
        responseResult.setData(headPortrait_url);
        return responseResult;
    }
}
