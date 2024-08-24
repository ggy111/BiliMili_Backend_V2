package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.Video;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.video.VideoService;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoService videoService;

    @MockBean
    private VideoMapper videoMapper;

    @MockBean
    private RedisTool redisTool;

    @MockBean
    private CurrentUser currentUser;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private VideoController videoController;


    @Test
    @WithMockUser
    @SneakyThrows
    void updateStatusP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("updateStatusP");
        responseResult.setMessage("更新成功");
        when(videoController.updateStatus(4,2)).thenReturn(responseResult);
        mockMvc.perform(post("/video/change/status")
                        .param("vid", String.valueOf(4))
                        .param("status", String.valueOf(2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新成功"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void updateStatusN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("updateStatusN");
        responseResult.setMessage("更新失败");
        responseResult.setCode(500);
        when(videoController.updateStatus(-1,2)).thenReturn(responseResult);
        mockMvc.perform(post("/video/change/status")
                        .param("vid", String.valueOf(-1))
                        .param("status", String.valueOf(2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新失败"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void randomVideosForVisitorP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("randomVideosForVisitorP");
        responseResult.setMessage("推荐成功");
        when(videoController.randomVideosForVisitor()).thenReturn(responseResult);
        mockMvc.perform(post("/video/random/visitor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("推荐成功"));
    }




    @Test
    @WithMockUser
    @SneakyThrows
    void cumulativeVideosForVisitorP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("cumulativeVideosForVisitorP");
        responseResult.setMessage("获取成功");
        when(videoController.cumulativeVideosForVisitor("4")).thenReturn(responseResult);
        mockMvc.perform(get("video/cumulative/visitor")
                        .param("vids", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }



    @Test
    @WithMockUser
    @SneakyThrows
    void cumulativeVideosForVisitorN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("cumulativeVideosForVisitorN");
        responseResult.setMessage("获取失败");
        responseResult.setCode(500);
        when(videoController.cumulativeVideosForVisitor("-1")).thenReturn(responseResult);
        mockMvc.perform(get("video/cumulative/visitor")
                        .param("vids", "-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取失败"));
    }





    @Test
    @WithMockUser
    @SneakyThrows
    void getOneVideoP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getOneVideoP");
        responseResult.setMessage("获取成功");
        when(videoController.getOneVideo(4)).thenReturn(responseResult);
        mockMvc.perform(get("video/getone")
                        .param("vid", String.valueOf(4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }




    @Test
    @WithMockUser
    @SneakyThrows
    void getOneVideoN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getOneVideoN");
        responseResult.setMessage("获取失败");
        responseResult.setCode(500);
        when(videoController.getOneVideo(-1)).thenReturn(responseResult);
        mockMvc.perform(get("video/getone")
                        .param("vid", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取失败"));
    }





    @Test
    @WithMockUser
    @SneakyThrows
    void getUserWorksP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getUserWorksP");
        responseResult.setMessage("获取成功");
        when(videoController.getUserWorks(5,1,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("video/user-works")
                        .param("uid", String.valueOf(5))
                        .param("rule", String.valueOf(1))
                        .param("page", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }




    @Test
    @WithMockUser
    @SneakyThrows
    void getUserWorksN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getUserWorksN");
        responseResult.setMessage("获取失败");
        responseResult.setCode(500);
        when(videoController.getUserWorks(-1,1,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("video/user-works")
                        .param("uid", String.valueOf(-1))
                        .param("rule", String.valueOf(1))
                        .param("page", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取失败"));
    }



    @Test
    @WithMockUser
    @SneakyThrows
    void getUserLoveMoviesP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getUserLoveMoviesP");
        responseResult.setMessage("获取成功");
        when(videoController.getUserLoveMovies(5,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("video/user-love")
                        .param("uid", String.valueOf(5))
                        .param("offset", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void getUserLoveMoviesN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getUserLoveMoviesN");
        responseResult.setMessage("获取失败");
        responseResult.setCode(500);
        when(videoController.getUserLoveMovies(-1,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("video/user-love")
                        .param("uid", String.valueOf(-1))
                        .param("offset", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void getUserCollectVideosP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getUserCollectVideosP");
        responseResult.setMessage("获取成功");
        when(videoController.getUserCollectVideos(6,1,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("video/user-collect")
                        .param("fid", String.valueOf(6))
                        .param("rule", String.valueOf(1))
                        .param("page", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void getUserCollectVideosN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getUserCollectVideosN");
        responseResult.setMessage("获取失败");
        responseResult.setCode(500);
        when(videoController.getUserCollectVideos(-1,1,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("video/user-collect")
                        .param("fid", String.valueOf(-1))
                        .param("rule", String.valueOf(1))
                        .param("page", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }


    //---------------------------------------------------

    @Test
    @WithMockUser
    public void updateStatusTest_chatGPT() throws Exception {
        int vid = 1;
        int status = 1;

        // Mock the response from videoService
        //when(videoService.changeVideoStatus(vid, status)).thenReturn(new ResponseResult(200, "Success", null));

        // Perform the POST request and check the result
        mockMvc.perform(post("/chatgpt/video/change/status")
                        .param("vid", String.valueOf(vid))
                        .param("status", String.valueOf(status)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    public void randomVideosForVisitorTest_chatGPT() throws Exception {
        List<Video> randomVideos = new ArrayList<>();
        //randomVideos.add(new Video(1, "Test Video 1", 1));
        //randomVideos.add(new Video(2, "Test Video 2", 1));

        // Mock the response from videoMapper and videoService
      //  when(videoMapper.selectCountVideoByRandom(1, 11)).thenReturn(randomVideos);
       // when(videoService.getVideosDataWithPageByVideoList(randomVideos, 1, 11)).thenReturn(new ArrayList<>());

        // Perform the GET request and check the result
        mockMvc.perform(get("/chatgpt/video/random/visitor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void getOneVideoTest_chatGPT() throws Exception {
        int vid = 1;
       // Video video = new Video(vid, "Test Video", 1);

        // Mock the response from videoService
      //  when(videoService.getVideoWithDataByVideoId(vid)).thenReturn(Map.of("video", video));

        // Perform the GET request and check the result
        mockMvc.perform(get("/chat_gpt/video/getone").param("vid", String.valueOf(vid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.video.vid").value(vid))
                .andExpect(jsonPath("$.data.video.title").value("Test Video"));
    }

    @Test
    @WithMockUser
    public void getUserPlayMoviesTest_chatGPT() throws Exception {
        int uid = 1;
        List<Video> videoList = new ArrayList<>();
        //videoList.add(new Video(1, "Test Video", 1));

        // Mock the response from currentUser and RedisTemplate
       // when(currentUser.getUserId()).thenReturn(uid);
        //when(redisTemplate.opsForZSet().reverseRange("user_video_history:" + uid, 0L, 9L))
          //      .thenReturn(Set.of(1));

        // Mock the videoService call
        //when(videoService.getVideosDataWithPageBySort(List.of(1), null, 1, 1)).thenReturn(List.of());

        // Perform the GET request and check the result
        mockMvc.perform(get("/chatGPT/video/user-play")
                        .param("offset", "0")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void getUserCollectVideosTest_chatGPT() throws Exception {
        int fid = 1;
        List<Video> videoList = new ArrayList<>();
        //videoList.add(new Video(1, "Test Video", 1));

        // Mock RedisTemplate response and videoService call
        //when(redisTemplate.opsForZSet().reverseRange("favorite_video:" + fid, 0, 9))
          //      .thenReturn(Set.of(1));
        //when(videoService.getVideosDataWithPageBySort(List.of(1), null, 1, 10))
            //    .thenReturn(new ArrayList<>());

        // Perform the GET request and check the result
        mockMvc.perform(get("chat_gpt/video/user-collect")
                        .param("fid", String.valueOf(fid))
                        .param("rule", "1")
                        .param("page", "1")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
