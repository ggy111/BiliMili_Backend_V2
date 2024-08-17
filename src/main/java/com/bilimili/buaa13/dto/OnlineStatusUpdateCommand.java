package com.bilimili.buaa13.dto;

import lombok.Getter;
import lombok.Setter;

public class OnlineStatusUpdateCommand {
    @Setter
    @Getter
    private Integer from;
    @Setter
    @Getter
    private Integer to;
    private boolean isOnline;

    public OnlineStatusUpdateCommand() {
    }

    public OnlineStatusUpdateCommand(Integer from, Integer to, boolean isOnline) {
        this.from = from;
        this.to = to;
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
