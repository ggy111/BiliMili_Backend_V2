package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.video.FavoriteVideoService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class FavoriteVideoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FavoriteVideoService favoriteVideoService;
    @MockBean
    private FavoriteVideoController favoriteVideoController;
    @Test
    @SneakyThrows
    @WithMockUser
    void getCollectedFidsP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getCollectedFidsP");
        responseResult.setCode(200);
        responseResult.setMessage("获取成功");
        when(favoriteVideoController.getCollectedFids(5)).thenReturn(responseResult);
        mockMvc.perform(get("/video/collected-fids")
                        .param("vid", String.valueOf(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取成功"));
    }
    @Test
    @SneakyThrows
    @WithMockUser
    void getCollectedFidsN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getCollectedFidsN");
        responseResult.setCode(404);
        responseResult.setMessage("获取失败");
        when(favoriteVideoController.getCollectedFids(-5)).thenReturn(responseResult);
        mockMvc.perform(get("/video/collected-fids")
                        .param("vid", String.valueOf(-5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("404"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("获取失败"));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void cancelCollectP() {
        Set<Integer> fids = new HashSet<>();
        fids.add(6);
        when(favoriteVideoService.findFidsOfCollected(4, Collections.singleton(6))).thenReturn(fids);
        mockMvc.perform(post("/video/cancel-collect")
                .param("vid", String.valueOf(4))
                .param("fid", String.valueOf(6)))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void cancelCollectN() {
        Set<Integer> fids = new HashSet<>();
        fids.add(6);
        when(favoriteVideoService.findFidsOfCollected(4, Collections.singleton(6))).thenReturn(fids);
        ResultActions resultActions =  mockMvc.perform(post("/video/cancel-collect")
                        .param("vid", String.valueOf(-3))
                        .param("fid", String.valueOf(-5)))
                .andExpect(status().isOk());;
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void collectVideoP() {
        Integer vid = 5;
        String[] addArray = new String[]{"5005", "5006", "5007"};
        String[] removeArray = new String[]{"5008", "5009", "5010"};
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("collectVideoP");
        responseResult.setCode(200);
        responseResult.setMessage("收藏成功");
        when(favoriteVideoController.collectVideo(vid,addArray,removeArray)).thenReturn(responseResult);
        mockMvc.perform(post("/video/collect")
                .param("vid", String.valueOf(vid))
                .param("adds",addArray)
                .param("removes",removeArray))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("收藏成功"));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void collectVideoN() {
        Integer vid = -5;
        String[] addArray = new String[]{"-5005", "-5006", "-5007"};
        String[] removeArray = new String[]{"-5008", "-5009", "-5010"};
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("collectVideoN");
        responseResult.setCode(404);
        responseResult.setMessage("收藏失败");
        when(favoriteVideoController.collectVideo(vid,addArray,removeArray)).thenReturn(responseResult);
        mockMvc.perform(post("/video/collect")
                        .param("vid", String.valueOf(vid))
                        .param("adds",addArray)
                        .param("removes",removeArray))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("404"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("收藏失败"));
    }
}