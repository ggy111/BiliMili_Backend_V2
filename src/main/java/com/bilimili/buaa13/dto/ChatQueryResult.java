package com.bilimili.buaa13.dto;

public class ChatQueryResult {
    private Integer chatId;
    private Integer userId;
    private String lastMessage;
    private String messageStatus;

    public ChatQueryResult() {
    }

    public ChatQueryResult(Integer chatId, Integer userId, String lastMessage, String messageStatus) {
        this.chatId = chatId;
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.messageStatus = messageStatus;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }
}
