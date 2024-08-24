package com.bilimili.buaa13.controller;


import com.bilimili.buaa13.entity.ResponseResult;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VideoStatusControllerTest {
    @Autowired
    private VideoStatusController videoStatusController;

    @Test
    @WithMockUser
    @SneakyThrows
    void newPlayWithVisitorP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("newPlayWithVisitorP");
        responseResult.setMessage("更新成功");
        responseResult.setCode(200);
        when(videoStatusController.newPlayWithVisitor(5)).thenReturn(responseResult);
        mockMvc.perform(post("/video/play/visitor")
                        .param("vid", String.valueOf(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新成功"));
    }



    @Test
    @WithMockUser
    @SneakyThrows
    void newPlayWithVisitorN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("newPlayWithVisitorN");
        responseResult.setMessage("更新失败");
        responseResult.setCode(500);
        when(videoStatusController.newPlayWithVisitor(-1)).thenReturn(responseResult);
        mockMvc.perform(post("/video/play/visitor")
                        .param("vid", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新失败"));
    }
}
