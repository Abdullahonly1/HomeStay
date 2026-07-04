package com.homestaybd.dto;

import java.time.LocalDateTime;

public class ReviewDTO {

    private Long id;
    private Long roomId;
    private String roomTitle;
    private Long userId;           // ← যোগ করো
    private String username;       // ← যোগ করো
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private boolean approved;      // ← যোগ করো

    private String guestUsername;     // ← এটা যোগ করো (গেস্টের নাম)
    // Default constructor
    public ReviewDTO() {}

    // Getters & Setters (সব ফিল্ডের জন্য)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomTitle() { return roomTitle; }
    public void setRoomTitle(String roomTitle) { this.roomTitle = roomTitle; }

    public Long getUserId() { return userId; }          // ← এটা দরকার
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }    // ← এটা দরকার
    public void setUsername(String username) { this.username = username; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isApproved() { return approved; }    // ← getter
    public void setApproved(boolean approved) { this.approved = approved; }  // ← setter দরকার


    public String getGuestUsername() {
        return guestUsername;
    }

    public void setGuestUsername(String guestUsername) {
        this.guestUsername = guestUsername;
    }
}