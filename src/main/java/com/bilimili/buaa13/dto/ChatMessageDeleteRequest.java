package com.bilimili.buaa13.dto;

public class ChatMessageDeleteRequest {
    private Integer messageId;

    public ChatMessageDeleteRequest() {
    }

    public ChatMessageDeleteRequest(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
}
