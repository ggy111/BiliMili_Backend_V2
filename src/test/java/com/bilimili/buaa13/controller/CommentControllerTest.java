package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.CommentTree;
import com.bilimili.buaa13.entity.CritiqueTree;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.chat.ChatDetailsService;
import com.bilimili.buaa13.service.chat.CurrentUserService;
import com.bilimili.buaa13.service.comment.CommentService;
import com.bilimili.buaa13.service.critique.CritiqueService;
import com.bilimili.buaa13.service.message.ChatDetailedService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.controller.CommentController;


import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.ibatis.javassist.expr.NewArray;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class)
public class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;
    @MockBean
    private CurrentUserService currentUserService;
    @MockBean
    private CurrentUser currentUser;
    @MockBean
    private ResponseResult responseResult;
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;
    @MockBean
    private CritiqueService critiqueService;
    @MockBean
    private RedisTool redisTool;
    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;
    @MockBean
    private CommentController commentController;

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetMoreCritiqueById_Success() throws Exception {
        // Mocking the service layer response
        CommentTree mockCommentTree = new CommentTree();
        // Populate the mockCommentTree with appropriate data
        Mockito.when(commentService.getMoreCommentsById(1)).thenReturn(mockCommentTree);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/comment/reply/get-more")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());// Replace with actual JSON structure validation

        // Verify that the service method was called with the correct parameters
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetMoreCritiqueById_NotFound() throws Exception {
        // Mocking the service layer to return null or throw an exception for not found
        Mockito.when(commentService.getMoreCommentsById(1)).thenReturn(null);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/comment/reply/get-more")
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testReportCritique_Failure() throws Exception {
        // Mock current user ID
        Integer mockUserId = 1;
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mock service behavior
        Mockito.when(critiqueService.reportCritique(123, 1, "Inappropriate content"))
               .thenReturn(true);

        // Perform the POST request
        mockMvc.perform(post("/comment/report")
                        .param("id", "123")
                        .param("reason", "Inappropriate content")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetCommentsByUser_WithComments() throws Exception {
        // Mock data
        List<CritiqueTree> mockComments = List.of(new CritiqueTree(), new CritiqueTree());
        ResponseResult responseResult = new ResponseResult();
        ArrayList<CritiqueTree> critiqueTreeArrayList = new ArrayList<>();
        responseResult.setData(critiqueTreeArrayList);

        // Mock service behavior
        Mockito.when(critiqueService.getCritiqueTreeByAid(1, 1L, 10))
               .thenReturn(mockComments);
        Mockito.when(commentController.getCommentsByUser(1, 1L, 10)).thenReturn(responseResult);

        // Perform the GET request
        mockMvc.perform(get("/comment/user/get")
                        .param("user_id", "1")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
