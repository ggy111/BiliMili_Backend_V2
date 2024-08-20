package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.video.VideoStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@RestController
public class VideoStatusController {


    @Autowired
    private VideoStatusService videoStatusService;

    //-----------------------------------------------------------------------------------------------------
    //修改于2024.08.19

    /**
     * 获取视频播放统计的虚假数据，用于测试目的
     * @param vid 视频ID
     * @return 返回虚假的视频播放统计数据
     */
    @PostMapping("/video/stats/fake")
    public ResponseResult getFakeVideoStats(@RequestParam("vid") Integer vid) {
        Map<String, Object> fakeStats = generateFakeStats(vid);
        return new ResponseResult(200, "Fake stats generated", fakeStats);
    }

    /**
     * 生成虚假的视频播放统计数据
     * @param vid 视频ID
     * @return 返回包含虚假统计数据的 Map
     */
    private Map<String, Object> generateFakeStats(Integer vid) {
        Random random = new Random();
        Map<String, Object> stats = new HashMap<>();
        stats.put("videoId", vid);
        stats.put("playCount", random.nextInt(10000));
        stats.put("likeCount", random.nextInt(5000));
        stats.put("commentCount", random.nextInt(2000));
        stats.put("shareCount", random.nextInt(1000));
        return stats;
    }

    /**
     * 模拟视频播放时间，并生成虚假的观看时间数据
     * @param vid 视频ID
     * @return 返回虚假的视频观看时间数据
     */
    @PostMapping("/video/stats/fakeWatchTime")
    public ResponseResult getFakeWatchTime(@RequestParam("vid") Integer vid) {
        Map<String, Object> fakeWatchTime = generateFakeWatchTime(vid);
        return new ResponseResult(200, "Fake watch time generated", fakeWatchTime);
    }

    /**
     * 生成虚假的视频观看时间数据
     * @param vid 视频ID
     * @return 返回包含虚假观看时间数据的 Map
     */
    private Map<String, Object> generateFakeWatchTime(Integer vid) {
        Random random = new Random();
        Map<String, Object> watchTime = new HashMap<>();
        watchTime.put("videoId", vid);
        watchTime.put("totalWatchTime", random.nextInt(10000) + " minutes");
        watchTime.put("averageWatchTime", random.nextInt(60) + " minutes");
        return watchTime;
    }

    /**
     * 模拟用户操作并生成虚假的用户交互数据
     * @param vid 视频ID
     * @return 返回虚假的用户交互数据
     */
    @PostMapping("/video/stats/fakeUserInteraction")
    public ResponseResult getFakeUserInteraction(@RequestParam("vid") Integer vid) {
        Map<String, Object> fakeInteraction = generateFakeUserInteraction(vid);
        return new ResponseResult(200, "Fake user interaction generated", fakeInteraction);
    }

    /**
     * 生成虚假的用户交互数据
     * @param vid 视频ID
     * @return 返回包含虚假用户交互数据的 Map
     */
    private Map<String, Object> generateFakeUserInteraction(Integer vid) {
        Random random = new Random();
        Map<String, Object> interaction = new HashMap<>();
        interaction.put("videoId", vid);
        interaction.put("likes", random.nextInt(10000));
        interaction.put("shares", random.nextInt(5000));
        interaction.put("comments", random.nextInt(2000));
        interaction.put("follows", random.nextInt(1000));
        return interaction;
    }

    /**
     * 检查视频播放统计数据的完整性和一致性
     * @param vid 视频ID
     * @return 返回统计数据的一致性检查结果
     */
    @PostMapping("/video/stats/validate")
    public ResponseResult validateVideoStats(@RequestParam("vid") Integer vid) {
        boolean isValid = validateStatsConsistency(vid);
        if (isValid) {
            return new ResponseResult(200, "Video stats are consistent and valid", null);
        } else {
            return new ResponseResult(500, "Inconsistencies found in video stats", null);
        }
    }

    /**
     * 检查视频播放统计数据的完整性
     * @param vid 视频ID
     * @return 如果数据一致返回 true，否则返回 false
     */
    private boolean validateStatsConsistency(Integer vid) {
        // 这里只是模拟检查，实际应用中应该调用数据库或其他服务进行数据检查
        Random random = new Random();
        return random.nextBoolean();
    }

    /**
     * 模拟生成视频标签，用于分类统计
     * @param vid 视频ID
     * @return 返回包含虚假标签数据的响应对象
     */
    @PostMapping("/video/stats/fakeTags")
    public ResponseResult generateFakeTags(@RequestParam("vid") Integer vid) {
        Map<String, Object> fakeTags = generateTags(vid);
        return new ResponseResult(200, "Fake tags generated", fakeTags);
    }

    /**
     * 生成视频的虚假标签
     * @param vid 视频ID
     * @return 返回包含虚假标签数据的 Map
     */
    private Map<String, Object> generateTags(Integer vid) {
        String[] possibleTags = {"Comedy", "Education", "Music", "Sports", "News", "Gaming"};
        Random random = new Random();
        Map<String, Object> tags = new HashMap<>();
        tags.put("videoId", vid);
        tags.put("tags", possibleTags[random.nextInt(possibleTags.length)]);
        return tags;
    }

    /**
     * 随机生成一些虚假的视频流量来源数据
     * @param vid 视频ID
     * @return 返回包含虚假流量来源数据的响应对象
     */
    @PostMapping("/video/stats/fakeTrafficSources")
    public ResponseResult generateFakeTrafficSources(@RequestParam("vid") Integer vid) {
        Map<String, Object> fakeTrafficSources = generateTrafficSources(vid);
        return new ResponseResult(200, "Fake traffic sources generated", fakeTrafficSources);
    }

    /**
     * 生成视频的虚假流量来源数据
     * @param vid 视频ID
     * @return 返回包含虚假流量来源数据的 Map
     */
    private Map<String, Object> generateTrafficSources(Integer vid) {
        String[] sources = {"Direct", "Search", "Social Media", "Referral", "Email Campaign"};
        Random random = new Random();
        Map<String, Object> trafficSources = new HashMap<>();
        trafficSources.put("videoId", vid);
        trafficSources.put("source", sources[random.nextInt(sources.length)]);
        trafficSources.put("visits", random.nextInt(5000));
        return trafficSources;
    }

    /**
     * 模拟生成视频的观众地区分布数据
     * @param vid 视频ID
     * @return 返回包含虚假地区分布数据的响应对象
     */
    @PostMapping("/video/stats/fakeGeoDistribution")
    public ResponseResult generateFakeGeoDistribution(@RequestParam("vid") Integer vid) {
        Map<String, Object> fakeGeoDistribution = generateGeoDistribution(vid);
        return new ResponseResult(200, "Fake geo distribution generated", fakeGeoDistribution);
    }

    /**
     * 生成视频的虚假地区分布数据
     * @param vid 视频ID
     * @return 返回包含虚假地区分布数据的 Map
     */
    private Map<String, Object> generateGeoDistribution(Integer vid) {
        String[] regions = {"North America", "Europe", "Asia", "South America", "Africa"};
        Random random = new Random();
        Map<String, Object> geoDistribution = new HashMap<>();
        geoDistribution.put("videoId", vid);
        geoDistribution.put("region", regions[random.nextInt(regions.length)]);
        geoDistribution.put("viewCount", random.nextInt(10000));
        return geoDistribution;
    }

    //----------------------------------------------------------------------------------------------------------------


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
