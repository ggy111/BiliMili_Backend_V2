package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.video.FavoriteVideoService;
import com.bilimili.buaa13.service.video.UserVideoService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserVideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserVideoService userVideoService;

    @MockBean
    private FavoriteVideoService favoriteVideoService;

    @MockBean
    private CurrentUser currentUser;
    @Autowired
    private UserVideoController userVideoController;


    @Test
    @WithMockUser
    @SneakyThrows
    void newPlayWithLoginUserP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("newPlayWithLoginUserP");
        responseResult.setMessage("更新成功");
        when(userVideoController.newPlayWithLoginUser(4)).thenReturn(responseResult);
        mockMvc.perform(post("/video/play/user")
                        .param("vid", String.valueOf(4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新成功"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void newPlayWithLoginUserN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("newPlayWithLoginUserN");
        responseResult.setMessage("更新失败");
        responseResult.setCode(500);
        when(userVideoController.newPlayWithLoginUser(-1)).thenReturn(responseResult);
        mockMvc.perform(post("/video/play/user")
                        .param("vid", String.valueOf(-1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新失败"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void loveOrNotP() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("loveOrNotP");
        responseResult.setMessage("更新赞踩成功");
        responseResult.setCode(200);
        when(userVideoController.loveOrNot(4,true,true)).thenReturn(responseResult);
        mockMvc.perform(post("/video/love-or-not")
                        .param("vid", String.valueOf(4))
                        .param("isLove", "true")
                        .param("isSet", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("200"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新赞踩成功"));
    }


    @Test
    @WithMockUser
    @SneakyThrows
    void loveOrNotN() {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("loveOrNotN");
        responseResult.setMessage("更新赞踩失败");
        responseResult.setCode(500);
        when(userVideoController.loveOrNot(-1,true,true)).thenReturn(responseResult);
        mockMvc.perform(post("/video/love-or-not")
                        .param("vid", String.valueOf(-1))
                        .param("isLove", "true")
                        .param("isSet", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value("500"))
                .andExpect(jsonPath("data").value(responseResult.getData()))
                .andExpect(jsonPath("message").value("更新赞踩失败"));
    }




    @Test
    @WithMockUser
    void newPlayWithLoginUserTest() throws Exception {
        // Mocking
        int uid = 1;
        int vid = 100;
        Set<Integer> fids = new HashSet<>();
        fids.add(uid + 5000);

        // Simulating service behavior
        when(currentUser.getUserId()).thenReturn(uid);
        when(userVideoService.updatePlay(uid, vid)).thenReturn("Play updated");

        // Mocking favorite service to add the video to favorites
        Mockito.doNothing().when(favoriteVideoService).addToFav(uid, vid, fids);

        // Performing the POST request and checking the response
        mockMvc.perform(post("/video/love-or-not")
                        .param("vid", String.valueOf(vid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Play updated"));
    }

    @Test
    @WithMockUser
    void loveOrNotTest_chatGPT() throws Exception {
        // Mocking
        int uid = 1;
        int vid = 100;
        boolean isLove = true;
        boolean isSet = true;

        // Simulating service behavior
        //when(currentUser.getUserId()).thenReturn(uid);
        //when(userVideoService.setLoveOrUnlove(uid, vid, isLove, isSet)).thenReturn("Love set");

        // Performing the POST request and checking the response
        mockMvc.perform(post("/chatgpt/video/love-or-not")
                        .param("vid", String.valueOf(vid))
                        .param("isLove", String.valueOf(isLove))
                        .param("isSet", String.valueOf(isSet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Love set"));
    }

}
