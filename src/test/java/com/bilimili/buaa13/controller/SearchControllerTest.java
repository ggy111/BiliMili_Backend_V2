package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.service.search.SearchService;
import com.bilimili.buaa13.service.video.VideoService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.mapper.PostMapper;
import com.bilimili.buaa13.service.post.PostService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SearchService searchService;
    @MockBean
    private SearchController searchController;
    @MockBean
    private VideoService videoService;
    @Test
    @SneakyThrows
    @WithMockUser
    void getMatchingVideo() {
        ResponseResult result = new ResponseResult();
        result.setCode(200);
        result.setMessage("获取匹配视频成功");
        result.setData("getMatchingVideo");
        when(searchController.getMatchingVideo("test2",1)).thenReturn(result);
        mockMvc.perform(get("/search/video/only-pass")
                .param("keyword","test2")
                .param("page","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取匹配视频成功"))
                .andExpect(jsonPath("$.data").value("getMatchingVideo"));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getMatchingVideoN() {
        ResponseResult result = new ResponseResult();
        result.setCode(404);
        result.setMessage("获取匹配视频失败");
        result.setData("getMatchingVideoN");
        List mcklist = Mockito.mock(List.class);
        List<Integer> vids = new ArrayList<>();
        vids.add(3);
        vids.add(4);
        vids.add(5);
        when(videoService.getVideosWithDataByVideoIdList(vids)).thenReturn(mcklist);
        mockMvc.perform(get("/search/video/only_pass")
                        .param("keyWord","test2")
                        .param("page","1"))
                .andExpect(status().isNotFound());
        Mockito.verify(videoService, times(0)).getVideosWithDataByVideoIdList(vids);
    }
}