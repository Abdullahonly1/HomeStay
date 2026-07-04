package com.homestaybd.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data                   // ← এটা থাকলে getter + setter অটো আসবে

@AllArgsConstructor
public class DashboardSummaryDTO {
   // private double earningsThisMonth;
    //private int upcomingCheckins;
    private int unreadMessages;
    private int pendingRequests;


    private double earningsThisMonth;         // ← এই ফিল্ডটা যোগ করো (double বা BigDecimal হতে পারে)
    private int upcomingCheckins;            // optional
    private int todayCheckins;               // optional
    private int tomorrowCheckins;            // optional

    public int getTodayCheckins() {
        return todayCheckins;
    }

    public void setTodayCheckins(int todayCheckins) {
        this.todayCheckins = todayCheckins;
    }

    public int getTomorrowCheckins() {
        return tomorrowCheckins;
    }

    public void setTomorrowCheckins(int tomorrowCheckins) {
        this.tomorrowCheckins = tomorrowCheckins;
    }



    // Default constructor
    public DashboardSummaryDTO() {}

    // Getters and Setters




    public int getUpcomingCheckins() {
        return upcomingCheckins;
    }

    public void setUpcomingCheckins(int upcomingCheckins) {
        this.upcomingCheckins = upcomingCheckins;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public int getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(int pendingRequests) {
        this.pendingRequests = pendingRequests;
    }
}