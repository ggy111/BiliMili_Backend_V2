package com.bilimili.buaa13.dto;

public class ChatDetailsRequest {
    private Integer uid;
    private Long offset;

    public ChatDetailsRequest() {
    }

    public ChatDetailsRequest(Integer uid, Long offset) {
        this.uid = uid;
        this.offset = offset;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }
}
