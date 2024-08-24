package com.bilimili.buaa13.controller;



import com.alibaba.druid.support.http.stat.WebAppStat;
import com.bilimili.buaa13.config.DruidConfig;
import com.bilimili.buaa13.config.WebSocketConfig;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.video.FavoriteService;
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
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserCommentControllerTest {
    @MockBean
    private UserCommentController userCommentController;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @SneakyThrows
    void getLikeAndDislikeP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("getFavoriteDetailsP");
        responseResult.setCode(200);
        responseResult.setMessage("更新成功");
        when(userCommentController.getLikeAndDislike()).thenReturn(responseResult);
        mockMvc.perform(get("/comment/get-like-and-dislike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()));
    }
}
