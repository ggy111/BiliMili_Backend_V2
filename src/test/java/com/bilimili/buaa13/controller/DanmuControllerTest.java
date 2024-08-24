package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.CommentTree;
import com.bilimili.buaa13.entity.CritiqueTree;
import com.bilimili.buaa13.entity.Danmu;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.mapper.DanmuMapper;
import com.bilimili.buaa13.mapper.UserMapper;
import com.bilimili.buaa13.mapper.VideoMapper;
import com.bilimili.buaa13.service.danmu.DanmuService;
import com.bilimili.buaa13.service.message.ChatDetailedService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.controller.CommentController;


import com.bilimili.buaa13.service.video.VideoReviewService;
import com.bilimili.buaa13.service.video.VideoStatusService;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DanmuController.class)
public class DanmuControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMapper userMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private DanmuMapper danmuMapper;
    @MockBean
    private VideoMapper videoMapper;
    @MockBean
    private VideoStatusService videoStatusService;
    @MockBean
    private VideoReviewService videoReviewService;
    @MockBean
    private DanmuService danmuService;
    @MockBean
    private RedisTool redisTool;
    @MockBean
    private CurrentUser currentUser;
    @MockBean
    private RedisTemplate<String, Object> redisTemplate;
    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;
    @Autowired
    private DanmuController danmuController;


    @Test
    @SneakyThrows
    @WithMockUser
    public void testDeleteDanmu_Success() {
        Integer mockId = 1;
        Integer mockLoginUid = 100;
        boolean isAdmin = true;

        when(currentUser.getUserId()).thenReturn(mockLoginUid);
        when(currentUser.isAdmin()).thenReturn(isAdmin);
        when(danmuService.deleteBarrage(mockId, mockLoginUid, isAdmin)).thenReturn(new ResponseResult(200, "Deleted Successfully", null));

        ResponseResult response = danmuController.deleteDanmu(mockId);

        verify(danmuService, times(1)).deleteBarrage(mockId, mockLoginUid, isAdmin);
        assertEquals(200, response.getCode());
        assertEquals("Deleted Successfully", response.getMessage());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testDeleteDanmu_IndividualBarrageList_NotAdmin() {

        Integer mockId = 1;
        Integer mockLoginUid = 100;
        boolean isAdmin = false;

        when(currentUser.getUserId()).thenReturn(mockLoginUid);
        when(currentUser.isAdmin()).thenReturn(isAdmin);
        when(danmuService.deleteBarrage(mockId, mockLoginUid, isAdmin)).thenReturn(new ResponseResult(403, "No Permission", null));

        ResponseResult response = danmuController.deleteDanmu(mockId);

        verify(danmuService, times(1)).deleteBarrage(mockId, mockLoginUid, isAdmin);
        assertEquals(403, response.getCode());
        assertEquals("No Permission", response.getMessage());
    }
}
