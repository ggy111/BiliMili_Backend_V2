package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.Category;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.message.ChatService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.category.CategoryService;
import com.bilimili.buaa13.mapper.CategoryMapper;
import com.bilimili.buaa13.service.video.VideoStatusService;
import com.bilimili.buaa13.service.user.UserService;


import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import lombok.SneakyThrows;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
public class ChatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @MockBean
    private CurrentUser currentUser;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisTool redisTool;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @MockBean
    private ZSetOperations<String, Object> zSetOperations;

    @Test
    @SneakyThrows
    @WithMockUser
    public void testCreateOneChat_NewChat() throws Exception {
        // Mocking current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mocking the service method for "新创建" scenario
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("msg", "新创建");
        Mockito.when(chatService.createOneChat(2, 1)).thenReturn(mockResult);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/msg/chat/create/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("新创建"))
                .andExpect(jsonPath("$.data").exists());

        // Verify that the service method was called
        Mockito.verify(chatService).createOneChat(2, 1);
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testCreateOneChat_UnknownUser() throws Exception {
        // Mocking current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mocking the service method for "未知用户" scenario
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("msg", "未知用户");
        Mockito.when(chatService.createOneChat(2, 1)).thenReturn(mockResult);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/msg/chat/create/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("未知用户"));

        // Verify that the service method was called
        Mockito.verify(chatService).createOneChat(2, 1);
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetRecentChatList_HasMoreData() throws Exception {
        // Mocking current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mocking Redis zCard method
        Mockito.when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Mockito.when(zSetOperations.zCard("chat_zset:1")).thenReturn(20L);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/msg/chat/recent-list")
                        .param("offset", "0"))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testDeleteOneChat_Success() throws Exception {
        // Mocking current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/msg/chat/delete/2"))
                .andExpect(status().isOk());

        // Verify that the service method was called with the correct parameters
        Mockito.verify(chatService).deleteOneChat(2, 1);
    }


    @Test
    @SneakyThrows
    @WithMockUser
    public void testUpdateStateOnline_Success() throws Exception {
        // Mocking current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/msg/chat/online")
                        .param("from", "2"))
                .andExpect(status().isOk());

        // Verify that the service method was called with the correct parameters
        Mockito.verify(chatService).updateStateOnline(2, 1);
    }
}
