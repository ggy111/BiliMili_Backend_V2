package com.bilimili.buaa13.service.chat;

import com.bilimili.buaa13.dto.ChatQueryResult;

import java.util.List;

public interface ChatQueryService {

    List<ChatQueryResult> findRecentChats(Integer userId, long offset);

    boolean hasMoreChats(Integer userId, long offset);
}
