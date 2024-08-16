package com.bilimili.buaa13.dto;

public class ChatCreationCommand {
    private Integer uid;

    public ChatCreationCommand() {
    }

    public ChatCreationCommand(Integer uid) {
        this.uid = uid;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }
}

