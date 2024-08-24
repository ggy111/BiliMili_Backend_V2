package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.dto.ChatDetailsRequest;
import com.bilimili.buaa13.dto.ChatMessageResponse;
import com.bilimili.buaa13.entity.Category;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.chat.ChatDetailsService;
import com.bilimili.buaa13.service.chat.CurrentUserService;
import com.bilimili.buaa13.service.message.ChatDetailedService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.user.FollowService;


import com.bilimili.buaa13.tools.JsonWebTokenTool;
import com.bilimili.buaa13.tools.RedisTool;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatDetailedController.class)
public class ChatDetailControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatDetailedService chatDetailedService;

    @MockBean
    private UserService userService;

    @MockBean
    private CurrentUser currentUser;

    @MockBean
    private RedisTool redisTool;

    @MockBean
    private JsonWebTokenTool jsonWebTokenTool;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private ChatDetailsService chatDetailsService;

    @MockBean
    private FollowService followService;

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetChatHistory_Failure() throws Exception {
        // Mocking the current user ID
        when(currentUserService.getCurrentUserId()).thenReturn(1);

        // Creating a mock request
        ChatDetailsRequest request = new ChatDetailsRequest(2, 0L);

        // Mocking the chat details service response
        List<ChatMessageResponse> mockMessages = List.of(
            new ChatMessageResponse()  // Fill with appropriate mock data
        );
        when(chatDetailsService.retrieveChatHistory(2, 1, 0L)).thenReturn(mockMessages);

        // Performing the POST request and verifying the result
        mockMvc.perform(post("/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }

    /*@Test
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
        Mockito.verify(followService).getUidFans(5,true);
    }*/

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetMoreChatDetails_Success() throws Exception {
        // Mocking the current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mocking the chat details service response
        Map<String, Object> mockMessages = new HashMap<>();
        mockMessages.put("messages", List.of(
            new ChatMessageResponse(),  // Fill with appropriate mock data
            new ChatMessageResponse()
        ));
        Mockito.when(chatDetailedService.getMessage(2, 1, 10L)).thenReturn(mockMessages);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/msg/chat-detailed/get-more")
                        .param("uid", "2")
                        .param("offset", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.data.messages").isNotEmpty());

        // Verify that the service method was called with the correct parameters
        Mockito.verify(chatDetailedService).getMessage(2, 1, 10L);
    }

    @Test
    @SneakyThrows
    @WithMockUser
    public void testGetMoreChatDetails_NoMessages() throws Exception {
        // Mocking the current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mocking the chat details service response with an empty map
        Map<String, Object> mockMessages = new HashMap<>();
        mockMessages.put("messages", List.of());
        Mockito.when(chatDetailedService.getMessage(2, 1, 10L)).thenReturn(mockMessages);

        // Performing the GET request and verifying the result
        mockMvc.perform(get("/msg/chat-detailed/get-more")
                        .param("uid", "2")
                        .param("offset", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.data.messages").isEmpty());

        // Verify that the service method was called with the correct parameters
        Mockito.verify(chatDetailedService).getMessage(2, 1, 10L);
    }


    @Test
    @SneakyThrows
    @WithMockUser
    public void testDelDetail_Failure() throws Exception {
        // Mocking the current user ID
        Mockito.when(currentUser.getUserId()).thenReturn(1);

        // Mocking the chat details service response to simulate a deletion failure
        Mockito.when(chatDetailedService.deleteChatDetail(100, 1)).thenReturn(false);

        // Performing the POST request and verifying the result
        mockMvc.perform(post("/msg/chat-detailed/delete")
                        .param("id", "100")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").doesNotExist())
                .andExpect(jsonPath("$.message").doesNotExist());
    }
}
