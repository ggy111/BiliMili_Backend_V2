package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.record.UserRecordService;
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
class UserRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRecordController userRecordController;

    @MockBean
    private UserRecordService userRecordService;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @MockBean
    private RedisTool redisTool;






    @Test
    @WithMockUser
    @SneakyThrows
    void getUserRecordP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(200);
        responseResult.setData("getUserRecordP");
        when(userRecordController.getUserRecord(5)).thenReturn(responseResult);
        mockMvc.perform(get("/user_record")
                        .param("uid", String.valueOf(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void getUserRecordN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(500);
        responseResult.setData("getUserRecordN");
        when(userRecordController.getUserRecord(-1)).thenReturn(responseResult);
        mockMvc.perform(get("/user_record")
                        .param("uid", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()));
    }
}
