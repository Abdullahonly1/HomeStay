package com.homestaybd.dto;

import lombok.Data;
import java.time.LocalDateTime;



@Data
public class NotificationDTO {
    private String type;              // "NEW_MESSAGE", "NEW_BOOKING" ইত্যাদি
    private String message;

    private String senderUsername;    // চ্যাট/মেসেজের জন্য (আগেরটা)
    private String guestUsername;     // ← এটা যোগ করো (বুকিং-এর জন্য গেস্টের নাম)

    private Long roomId;
    private Long bookingId;           // optional
    private LocalDateTime timestamp;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    public String getGuestUsername() {
        return guestUsername;
    }

    public void setGuestUsername(String guestUsername) {
        this.guestUsername = guestUsername;
    }
}