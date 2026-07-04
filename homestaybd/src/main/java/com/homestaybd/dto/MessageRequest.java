package com.homestaybd.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageRequest {
    private Long roomId;
    private String content;


    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

