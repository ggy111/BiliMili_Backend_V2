package com.bilimili.buaa13.service.chat;


import com.bilimili.buaa13.dto.ChatCreationCommand;
import com.bilimili.buaa13.dto.ChatQueryResult;
import com.bilimili.buaa13.dto.OnlineStatusUpdateCommand;

public interface ChatCommandService {

    ChatQueryResult createChat(ChatCreationCommand command, Integer userId);

    void removeChat(Integer uid, Integer userId);

    void updateUserOnlineStatus(OnlineStatusUpdateCommand command);

    void updateUserOfflineStatus(OnlineStatusUpdateCommand command);
}
