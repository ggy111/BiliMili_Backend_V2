package com.bilimili.buaa13.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bilimili.buaa13.entity.VideoStatus;
import com.bilimili.buaa13.mapper.VideoStatusMapper;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.tools.RedisTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class VideoStatusServiceImpl implements VideoStatusService {
    @Autowired
    private VideoStatusMapper videoStatusMapper;
    @Autowired
    private RedisTool redisTool;
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据视频ID查询视频常变数据
     * @param vid 视频ID
     * @return 视频数据统计
     */
    @Override
    public VideoStatus getStatusByVideoId(Integer vid) {
        //1注释Redis
        VideoStatus videoStatus = redisTool.getObject("videoStatus:" + vid, VideoStatus.class);
        if (videoStatus == null) {
            videoStatus = videoStatusMapper.selectById(vid);
            if (videoStatus != null) {
                VideoStatus finalVideoStatus = videoStatus;
                CompletableFuture.runAsync(() -> {
                    redisTool.setExObjectValue("videoStatus:" + vid, finalVideoStatus);    // 异步更新到redis
                }, taskExecutor);
            } else {
                return null;
            }
        }
        return videoStatusMapper.selectById(vid);
    }

    /**
     * 更新指定字段
     * @param vid   视频ID
     * @param column    对应数据库的列名
     * @param increase  是否增加，true则增加 false则减少
     * @param count 增减数量 一般是1，只有投币可以加2
     */
    @Override
    public void updateVideoStatus(Integer vid, String column, boolean increase, Integer count) {
        UpdateWrapper<VideoStatus> updateWrapper = new UpdateWrapper<>();
        if (increase) {
            updateWrapper.eq("vid", vid);
            updateWrapper.setSql(column + " = " + column + " + " + count);
        } else {
            // 更新后的字段不能小于0
            updateWrapper.eq("vid", vid);
            updateWrapper.setSql(column + " = CASE WHEN " + column + " - " + count + " < 0 THEN 0 ELSE " + column + " - " + count + " END");
        }
        videoStatusMapper.update(null, updateWrapper);
        //1注释Redis
        redisTool.deleteValue("videoStats:" + vid);
    }

    /**
     * 同时更新点赞和点踩
     * @param vid   视频ID
     * @param addGood   是否点赞，true则good+1&bad-1，false则good-1&bad+1
     */
    @Override
    public void updateGoodAndBad(Integer vid, boolean addGood) {
        UpdateWrapper<VideoStatus> updateWrapper = new UpdateWrapper<>();
        if (addGood) {
            updateWrapper.eq("vid", vid);
            updateWrapper.setSql("good = good + 1");
            updateWrapper.setSql("bad = CASE WHEN bad - 1 < 0 THEN 0 ELSE bad - 1 END");
        } else {
            updateWrapper.eq("vid", vid);
            updateWrapper.setSql("bad = bad + 1");
            updateWrapper.setSql("good = CASE WHEN good - 1 < 0 THEN 0 ELSE good - 1 END");
        }
        videoStatusMapper.update(null, updateWrapper);
        //1注释Redis
        redisTool.deleteValue("videoStats:" + vid);
    }
}
