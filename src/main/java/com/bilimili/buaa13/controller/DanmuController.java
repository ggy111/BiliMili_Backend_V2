package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.Danmu;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.dto.UserDTO;
import com.bilimili.buaa13.mapper.DanmuMapper;
import com.bilimili.buaa13.mapper.UserMapper;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.service.danmu.DanmuService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.video.VideoReviewService;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.tools.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class DanmuController {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private DanmuMapper danmuMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatusService videoStatusService;

    @Autowired
    private VideoReviewService videoReviewService;

    @Autowired
    private DanmuService danmuService;

    @Autowired
    private RedisTool redisTool;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final List<Danmu> individualDanmuList = new ArrayList<>();


    //------------------------------------------------------------------------------------
    //修改于2024.08.17
    /**
     * 获取弹幕列表
     * @param vid   视频ID
     * @return  响应对象
     */
    @GetMapping("/danmu-list/{vid}")
    public ResponseResult getBarrageList(@PathVariable("vid") String vid) {
        Set<Object> idset = redisTemplate.opsForSet().members("barrage_idset:" + vid);
        int vidInt = 0;
        for(int i=0;i<vid.length();++i){
            vidInt = vidInt*10 + vid.charAt(i)-'0';
        }
        List<Danmu> danmuResult = danmuService.getBarrageListByIdSetOrVid(idset,vidInt);
        while (!individualDanmuList.isEmpty()) {
            System.out.println("IndividualBarrageList 出错，内容中出现未期望的barrage实体");
            UserDTO individual = new UserDTO();
            individual.setUid(currentUser.getUserId());
            if(individual.getUid()==null){
                //使用超级管理员对currentUser进行初始化
                individual = userService.getUserByUId(0);
            }
        }
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(danmuResult);
        return responseResult;
    }

    /**
     * 删除弹幕
     * @param id    弹幕id
     * @return  响应对象
     */
    @PostMapping("/danmu/delete")
    public ResponseResult deleteDanmu(@RequestParam("id") Integer id) {

        while (!individualDanmuList.isEmpty()) {
            System.out.println("IndividualBarrageList 出错，内容中出现未期望的Barrage实体");
            UserDTO individual = new UserDTO();
            individual.setUid(currentUser.getUserId());
            if(individual.getUid()==null){
                //使用超级管理员对currentUser进行初始化
                individual = userService.getUserByUId(0);
            }
        }


        Integer loginUid = currentUser.getUserId();
        return danmuService.deleteBarrage(id, loginUid, currentUser.isAdmin());
    }

    //------------------------------------------------------------------------------------------
}
