package com.bilimili.buaa13.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {
    private Integer id;
    private String content;
    private LocalDateTime timestamp;
    private String sender;

    public ChatMessageResponse() {
    }

    public ChatMessageResponse(Integer id, String content, LocalDateTime timestamp, String sender) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.sender = sender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
