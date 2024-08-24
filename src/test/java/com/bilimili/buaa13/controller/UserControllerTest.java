package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.user.UserService;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserController userController;

    @MockBean
    private UserService userService;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @MockBean
    private RedisTool redisTool;


    @Test
    @WithMockUser
    @SneakyThrows
    void updateUserInfoP(){
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setData("upUserInfoP");
        responseResult.setMessage("更新成功");
        when(userController.updateUserInfo("testName","testDescription",1)).thenReturn(responseResult);
        mockMvc.perform(post("/user/info/update")
                        .param("nickname", "testName")
                        .param("description","testDescription")
                        .param("gender","1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新成功"));
    }

    @Test
    @WithMockUser
    @SneakyThrows
    void updateUserInfoN(){
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(500);
        responseResult.setData("upUserInfoP");
        responseResult.setMessage("更新失败");
        when(userController.updateUserInfo("testName","testDescription",-1)).thenReturn(responseResult);
        mockMvc.perform(post("/user/info/update")
                        .param("nickname", "testName")
                        .param("description","testDescription")
                        .param("gender","-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新失败"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void getOneUserInfoP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getOneUserInfoP");
        when(userController.getOneUserInfo(5)).thenReturn(responseResult);
        mockMvc.perform(get("/user/info/get-one")
                        .param("uid", String.valueOf(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()));
    }



    @Test
    @WithMockUser
    @SneakyThrows
    void getOneUserInfoN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getOneUserInfoN");
        responseResult.setCode(500);
        when(userController.getOneUserInfo(-1)).thenReturn(responseResult);
        mockMvc.perform(get("/user/info/get-one")
                        .param("uid", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()));
    }
}
