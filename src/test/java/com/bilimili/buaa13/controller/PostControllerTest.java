package com.bilimili.buaa13.controller;

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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PostService postService;
    @MockBean
    private PostController postController;
    @MockBean
    private PostMapper postMapper;

    @Test
    @SneakyThrows
    @WithMockUser
    void addAllArticle() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setMessage("add article success");
        responseResult.setData("addAllArticle");
        when(postController.addAllArticle(5,"post测试")).thenReturn(responseResult);
        mockMvc.perform(post("/post/add")
                .param("uid",String.valueOf(5))
                .param("content","post测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("add article success"))
                .andExpect(jsonPath("data").value("addAllArticle"));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void addAllArticleN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(404);
        responseResult.setMessage("add article fail");
        responseResult.setData("addAllArticleN");
        when(postController.addAllArticle(-5,"post测试")).thenReturn(responseResult);
        mockMvc.perform(post("/post/add")
                        .param("uId",String.valueOf(-5))
                        .param("content","post测试"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getOneUserInfo() {
        when(postService.getPostsByIds(5)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/post/get")
                .param("uid",String.valueOf(5)))
                .andExpect(status().isOk());
        Mockito.verify(postService,times(0)).getPostsByIds(5);
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getOneUserInfoN() {
        when(postService.getPostsByIds(-5)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/postN/get")
                        .param("uid",String.valueOf(-5)))
                .andExpect(status().isNotFound());
        Mockito.verify(postService,times(0)).getPostsByIds(5);
    }
}