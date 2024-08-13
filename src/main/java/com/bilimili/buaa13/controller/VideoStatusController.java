package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.video.VideoStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoStatusController {
    @Autowired
    private VideoStatusService videoStatusService;

    /**
     * 游客观看视频时更新视频播放量数据，这个做不到时间间隔，就是说每次刷新都会播放数加一，有一个思路是使用浏览器指纹，但是我不会
     * @param vid 视频ID
     * @return
     */
    @PostMapping("/video/play/visitor")
    public ResponseResult newPlayWithVisitor(@RequestParam("vid") Integer vid) {
        videoStatusService.updateVideoStatus(vid, "play", true, 1);
        return new ResponseResult();
    }
}
