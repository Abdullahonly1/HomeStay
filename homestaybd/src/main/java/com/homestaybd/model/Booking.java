package com.homestaybd.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private int numberOfPersons;

    private double totalPrice;

    @Enumerated(EnumType.STRING)           // ← এই লাইন যোগ করা হয়েছে
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;          // String → BookingStatus enum-এ পরিবর্তন



    @Column(name = "created_at")
    private LocalDateTime createdAt;   // ← এটা যোগ করো

    // যদি Lombok না ব্যবহার করো তাহলে ম্যানুয়ালি getter/setter লিখো
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // যদি চাও তাহলে default value দিতে পারো
    // @PrePersist
    // private void prePersist() {
    //     if (this.status == null) {
    //         this.status = BookingStatus.PENDING;
    //     }
    // }
}