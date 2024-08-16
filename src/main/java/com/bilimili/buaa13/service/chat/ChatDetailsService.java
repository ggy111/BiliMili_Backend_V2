package com.bilimili.buaa13.service.chat;



import com.bilimili.buaa13.dto.ChatMessageResponse;

import java.util.List;

public interface ChatDetailsService {

    List<ChatMessageResponse> retrieveChatHistory(Integer uid, Integer currentUserId, Long offset);

    void removeMessage(Integer messageId, Integer currentUserId);
}

