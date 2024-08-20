package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.message.ChatDetailedService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bilimili.buaa13.dto.ChatDetailsRequest;
import com.bilimili.buaa13.dto.ChatMessageDeleteRequest;
import com.bilimili.buaa13.dto.ChatMessageResponse;
import com.bilimili.buaa13.service.chat.ChatDetailsService;
import com.bilimili.buaa13.service.chat.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatDetailedController {
    @Autowired
    private ChatDetailedService chatDetailedService;

    @Autowired
    private UserService userService;



    @Autowired
    private CurrentUser currentUser;


    //-----------------------------------------------------------------------------------------
    //修改于2024.08.16

    private final ChatDetailsService chatDetailsService = new ChatDetailsService() {
        @Override
        public List<ChatMessageResponse> retrieveChatHistory(Integer uid, Integer currentUserId, Long offset) {
            return List.of();
        }

        @Override
        public void removeMessage(Integer messageId, Integer currentUserId) {

        }
    };
    private final CurrentUserService currentUserService = new CurrentUserService() {
        @Override
        public Integer getCurrentUserId() {
            return 0;
        }
    };

    /**
     * 获取更多历史消息
     *
     * @param request ChatDetailsRequest 包含聊天对象的UID和偏移量
     * @return 响应对象，包含更多消息记录
     */
    @PostMapping("/history")
    public ResponseResult getChatHistory1(@RequestBody ChatDetailsRequest request) {
        Integer currentUserId = currentUserService.getCurrentUserId();
        List<ChatMessageResponse> messages = chatDetailsService.retrieveChatHistory(request.getUid(), currentUserId, request.getOffset());
        return new ResponseResult(HttpStatus.OK.value(), "History retrieved successfully", messages);
    }

    /**
     * 删除单条消息
     *
     * @param request ChatMessageDeleteRequest 包含要删除的消息ID
     * @return 响应对象
     */
    @DeleteMapping("/delete")
    public ResponseResult deleteChatMessage1(@RequestBody ChatMessageDeleteRequest request) {
        Integer currentUserId = currentUserService.getCurrentUserId();
        chatDetailsService.removeMessage(request.getMessageId(), currentUserId);
        return new ResponseResult(HttpStatus.NO_CONTENT.value(), "Message deleted successfully",null);
    }

    //-----------------------------------------------------------------------------------------


    /**
     * 获取更多历史消息记录
     * @param uid   聊天对象的UID
     * @param offset    偏移量，即已经获取过的消息数量，从哪条开始获取更多
     * @return  响应对象，包含更多消息记录的map
     */
    @GetMapping("/msg/chat-detailed/get-more")
    public ResponseResult getMoreChatDetails(@RequestParam("uid") Integer uid,
                                             @RequestParam("offset") Long offset) {
        Integer loginUid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData(chatDetailedService.getMessage(uid, loginUid, offset));
        return responseResult;
    }

    /**
     * 删除消息
     * @param id    消息ID
     * @return  响应对象
     */
    @PostMapping("/msg/chat-detailed/delete")
    public ResponseResult delDetail(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        ResponseResult responseResult = new ResponseResult();
        if (!chatDetailedService.deleteChatDetail(id, loginUid)) {
            responseResult.setCode(500);
            responseResult.setMessage("删除消息失败");
        }
        return responseResult;
    }
}
