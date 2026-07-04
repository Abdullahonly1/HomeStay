package com.homestaybd.dto;

import java.time.LocalDateTime;

public class ChatRoomDTO {

    private Long roomId;
    private String roomTitle;
    private String roomImage;           // ← এটা যোগ করো
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private long unreadCount;

    // Constructors
    public ChatRoomDTO() {}

    // Getters & Setters
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public String getRoomImage() {      // ← এটা যোগ করো
        return roomImage;
    }

    public void setRoomImage(String roomImage) {   // ← এটা যোগ করো
        this.roomImage = roomImage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}