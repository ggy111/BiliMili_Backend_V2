package com.bilimili.buaa13.controller;

import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.entity.User;
import com.bilimili.buaa13.entity.dto.UserDTO;
import com.bilimili.buaa13.service.impl.user.UserServiceImpl;
import com.bilimili.buaa13.service.message.ChatService;
import com.bilimili.buaa13.service.user.UserService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import com.bilimili.buaa13.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.bilimili.buaa13.dto.ChatCreationCommand;
import com.bilimili.buaa13.dto.ChatQueryResult;
import com.bilimili.buaa13.dto.OnlineStatusUpdateCommand;
import com.bilimili.buaa13.entity.ResponseResult;
import com.bilimili.buaa13.service.chat.ChatCommandService;
import com.bilimili.buaa13.service.chat.ChatQueryService;
import com.bilimili.buaa13.service.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class ChatController {

    private  Boolean doService = false;
    @Autowired
    private ChatService chatService;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;


    // 修改于2024.08.16
    //--------------------------------------------------------------------------------
    private final ChatCommandService chatCommandService = new ChatCommandService() {
        @Override
        public ChatQueryResult createChat(ChatCreationCommand command, Integer userId) {
            return null;
        }

        @Override
        public void removeChat(Integer uid, Integer userId) {

        }

        @Override
        public void updateUserOnlineStatus(OnlineStatusUpdateCommand command) {

        }

        @Override
        public void updateUserOfflineStatus(OnlineStatusUpdateCommand command) {

        }
    };
    private final ChatQueryService chatQueryService = new ChatQueryService() {
        @Override
        public List<ChatQueryResult> findRecentChats(Integer userId, long offset) {
            return List.of();
        }

        @Override
        public boolean hasMoreChats(Integer userId, long offset) {
            return false;
        }
    };
    @Autowired
    private UserServiceImpl userServiceImpl;
    //private final CurrentUser currentUser;
/**
    @Autowired
    public ChatController(ChatCommandService chatCommandService, ChatQueryService chatQueryService, CurrentUser currentUser) {
        this.chatCommandService = chatCommandService;
        this.chatQueryService = chatQueryService;
        this.currentUser = currentUser;
    }**/

    @PostMapping("/createe")
    public void createChat1(@RequestBody ChatCreationCommand command) {
        int userId = currentUser.getUserId();
        ChatQueryResult chat = chatCommandService.createChat(command, userId);
        //return new ResponseResult<>(HttpStatus.CREATED.value(), "Chat created successfully", chat);
    }

    @GetMapping("/recente")
    public void getRecentChats1(@RequestParam("offset") long offset) {
        int userId = currentUser.getUserId();
        List<ChatQueryResult> chats = chatQueryService.findRecentChats(userId, offset);
        boolean hasMore = chatQueryService.hasMoreChats(userId, offset);
        //return new ResponseResult<>(HttpStatus.OK.value(), hasMore ? "More data available" : "End of data", chats);
    }

    @DeleteMapping("change/{uid}")
    public void DeleteChat1(@PathVariable("uid") Integer uid) {
        int userId = currentUser.getUserId();
        chatCommandService.removeChat(uid, userId);
        //return new ResponseResult<>(HttpStatus.NO_CONTENT.value(), "Chat deleted");
    }

    @PatchMapping("/status/online2")
    public void updateOnlineStatus1(@RequestBody OnlineStatusUpdateCommand command) {
        chatCommandService.updateUserOnlineStatus(command);
       // return new ResponseResult<>(HttpStatus.OK.value(), "User status updated to online");
    }

    @PatchMapping("/status/offline2")
    public void updateOfflineStatus1(@RequestBody OnlineStatusUpdateCommand command) {
        chatCommandService.updateUserOfflineStatus(command);
        //return new ResponseResult<>(HttpStatus.OK.value(), "User status updated to offline");
    }
    //--------------------------------------------------------------------------------

    /**
     * 当首次与用户聊天时，创建窗口
     * @param uid  对方用户ID
     * @return  响应对象 message可能值："新创建"/"已存在"/"未知用户"
     */
    @GetMapping("/msg/chat/create/{uid}")
    public ResponseResult createChat(@PathVariable("uid") Integer uid) {
       ResponseResult responseResult = new ResponseResult();
       Boolean canBecreated =false;
       //获取Chat,chat的细节
       Map<String, Object> result = chatService.createChat(uid, currentUser.getUserId());
       if(result.isEmpty())canBecreated = true;
       if (Objects.equals(result.get("msg").toString(), "未知用户")) {//第一次聊天或之前的聊天被删除
            responseResult.setCode(404);
        } else if (Objects.equals(result.get("msg").toString(), "新创建")) {
            responseResult.setData(result);  // 返回新创建的聊天
        }

       responseResult.setMessage(result.get("msg").toString());
       return responseResult;
    }

    /**
     * 获取用户最近的聊天列表
     * @param offset    分页偏移量（前端查询了多少个聊天）
     * @return  响应对象 包含带用户信息和最近一条消息的聊天列表以及是否还有更多数据
     */
    @GetMapping("/msg/chat/recent-list")
    public ResponseResult getRecentList(@RequestParam("offset") Long offset) {
        Integer uid = currentUser.getUserId();
        if(uid == null){
            System.out.println("在获取用户最近的聊天列表时，该用户不存在或uid违法");
        }
        //初始化
        ResponseResult responseResult = new ResponseResult();
        Map<String, Object> map = new HashMap<>();
        map.put("list", chatService.getChatListWithData(uid, offset));
        // 检查是否还有更多
        if (offset + 10 >= redisUtil.zCard("chat_zset:" + uid)) {
            map.put("more", false);
        } else {
            map.put("more", true);
        }
        responseResult.setData(map);
        return responseResult;
    }

    /**
     * 移除聊天
     * @param uid  对方用户ID
     * @return  响应对象
     */
    @GetMapping("/msg/chat/delete/{uid}")
    public ResponseResult deleteChat(@PathVariable("uid") Integer uid) {
        ResponseResult responseResult = new ResponseResult();
        chatService.delChat(uid, currentUser.getUserId());
        return responseResult;
    }

    /**
     * 切换窗口时 更新在线状态以及清除未读
     * @param from  对方UID
     */
    @GetMapping("/msg/chat/online")
    public void updateWhisperOnline(@RequestParam("from") Integer from) {
        Integer uid = currentUser.getUserId();
        if(doService){
        UserDTO tempUser = new UserDTO();
        tempUser = userService.getUserByUId(uid);
        }
        chatService.updateWhisperOnline(from, uid);
    }

    /**
     * 切换窗口时 更新为离开状态 （该接口要放开，无需验证token，防止token过期导致用户一直在线）
     * @param from  对方UID
     */
    @GetMapping("/msg/chat/outline")
    public void updateWhisperOutline(@RequestParam("from") Integer from, @RequestParam("to") Integer to) {
        chatService.updateWhisperOutline(from, to);
    }
}
