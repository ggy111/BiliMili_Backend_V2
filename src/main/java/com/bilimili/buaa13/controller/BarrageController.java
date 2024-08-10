package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.Barrage;
import com.bilimili.buaa13.service.barrage.BarrageService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
public class BarrageController {
    @Autowired
    private BarrageService barrageService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 获取弹幕列表
     * @param vid   视频ID
     * @return  响应对象
     */
    @GetMapping("/danmu-list/{vid}")
    public ResponseResult getBarrageList(@PathVariable("vid") String vid) {
        Set<Object> idset = redisUtil.getMembers("barrage_bidSet:" + vid);
        int vidInt = 0;
        for(int i=0;i<vid.length();++i){
            vidInt = vidInt*10 + vid.charAt(i)-'0';
        }
        List<Barrage> barrageList = barrageService.getBarrageListByIdSetOrVid(idset, vidInt);
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(barrageList);
        return responseResult;
    }

    /**
     * 删除弹幕
     * @param id    弹幕id
     * @return  响应对象
     */
    @PostMapping("/danmu/delete")
    public ResponseResult deleteBarrage(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return barrageService.deleteBarrage(id, loginUid, currentUser.isAdmin());
    }
}
