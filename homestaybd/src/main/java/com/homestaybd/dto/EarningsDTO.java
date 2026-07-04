package com.homestaybd.dto;

public class EarningsDTO {
    private String month;   // যেমন: "Jan 2026" বা "2026-01"
    private double amount;

    public EarningsDTO() {}

    public EarningsDTO(String month, double amount) {
        this.month = month;
        this.amount = amount;
    }

    // Getters & Setters
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}