package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.user.FollowService;
import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
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
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FollowService followService;
    @MockBean
    private FollowController followController;
    @MockBean
    private RedisTool redisTool;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;
    @Test
    @SneakyThrows
    @WithMockUser
    void isFans() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setMessage("判断成功");
        responseResult.setData("isFans");
        List<Integer> mockList = new ArrayList<>();
        mockList.add(6);
        when(followService.getUidFans(5,true)).thenReturn(mockList);
        mockMvc.perform(get("/isFans")
                        .param("uidFollow", String.valueOf(5))
                        .param("uidFans", String.valueOf(6)))
                .andExpect(status().isOk());
        Mockito.verify(followService,times(0)).getUidFans(5,true);
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void isFansN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(404);
        responseResult.setMessage("判断失败");
        responseResult.setData("isFansN");
        List<Integer> mockList = new ArrayList<>();
        mockList.add(-6);
        when(followService.getUidFans(-5,true)).thenReturn(mockList);
        mockMvc.perform(get("/isFans")
                        .param("uidFollow", String.valueOf(-5))
                        .param("uidFans", String.valueOf(-6)))
                .andExpect(status().isOk());
        Mockito.verify(followService,times(0)).getUidFans(-5,true);
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void addFollowing() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setMessage("添加成功");
        responseResult.setData("addFollowing");
        when(followController.addFollowing(5,6,true)).thenReturn(responseResult);
        mockMvc.perform(post("/following/update")
                .param("uidFollow", String.valueOf(5))
                .param("uidFans", String.valueOf(6))
                .param("isfollowing", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("添加成功"))
                .andExpect(jsonPath("$.data").value("addFollowing"));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void addFollowingN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(404);
        responseResult.setMessage("添加失败");
        responseResult.setData("addFollowingN");
        when(followController.addFollowing(-5,-6,false)).thenReturn(responseResult);
        mockMvc.perform(post("/following/update")
                        .param("uidFollow", String.valueOf(-5))
                        .param("uidFans", String.valueOf(-6))
                        .param("isfollowing", String.valueOf(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("添加失败"))
                .andExpect(jsonPath("$.data").value("addFollowingN"));
    }
}