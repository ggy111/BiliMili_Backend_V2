package com.bilimili.buaa13.controller;


import com.alibaba.druid.support.http.stat.WebAppStat;
import com.bilimili.buaa13.config.DruidConfig;
import com.bilimili.buaa13.config.WebSocketConfig;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.video.FavoriteService;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VideoReviewControllerTest {
    @MockBean
    private VideoReviewController videoReviewController;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @SneakyThrows
    void getVideosP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getVidoesP");
        responseResult.setMessage("查询成功");
        responseResult.setCode(200);
        when(videoReviewController.getVideos(0,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("/review/video/getpage")
                        .param("vstatus", String.valueOf(0))
                        .param("page", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("查询成功"));
    }



    @Test
    @WithMockUser
    @SneakyThrows
    void getVideosN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getVidoesN");
        responseResult.setMessage("查询失败");
        responseResult.setCode(500);
        when(videoReviewController.getVideos(-1,1,1)).thenReturn(responseResult);
        mockMvc.perform(get("/review/video/getpage")
                        .param("vstatus", String.valueOf(-1))
                        .param("page", String.valueOf(1))
                        .param("quantity", String.valueOf(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("查询失败"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void getOneVideoP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getOneVidoeP");
        responseResult.setMessage("查询成功");
        responseResult.setCode(200);
        when(videoReviewController.getOneVideo(5)).thenReturn(responseResult);
        mockMvc.perform(get("/review/video/getone")
                        .param("vid", String.valueOf(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("查询成功"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void getOneVideoN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getOneVidoeN");
        responseResult.setMessage("查询失败");
        responseResult.setCode(500);
        when(videoReviewController.getOneVideo(-1)).thenReturn(responseResult);
        mockMvc.perform(get("/review/video/getone")
                        .param("vid", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("查询失败"));
    }



}
