package com.homestaybd.dto;

import com.homestaybd.model.Room;
import jakarta.persistence.ElementCollection;

import java.util.ArrayList;
import java.util.List;

public class RoomDTO {

    private Long id;
    private String title;
    private String description;
    private String facilities;
    private String location;
    private double price;
    private String imagePath;
    private boolean available;
    private Long hostId;           // host এর id দাও (User পুরোটা না)
    private List<String> additionalImages = new ArrayList<>();  // ← এটা থাকতে হবে

    public RoomDTO() {}

    public RoomDTO(Room room) {
        this.id = room.getId();
        this.title = room.getTitle();
        this.description = room.getDescription();
        this.facilities = room.getFacilities();
        this.location = room.getLocation();
        this.price = room.getPrice();
        this.imagePath = room.getImagePath();
        this.available = room.isAvailable();

        if (room.getHost() != null) {
            this.hostId = room.getHost().getId();
        }

        // সবচেয়ে গুরুত্বপূর্ণ লাইন
        if (room.getAdditionalImages() != null) {
            this.additionalImages = new ArrayList<>(room.getAdditionalImages());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public List<String> getAdditionalImages() {
        return additionalImages;
    }

    public void setAdditionalImages(List<String> additionalImages) {
        this.additionalImages = additionalImages;
    }


    // getters & setters (লোম্বক দিয়ে করতে পারো)


    // এই static method যোগ করো
    public static RoomDTO fromEntity(Room room) {
        if (room == null) return null;

        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setTitle(room.getTitle());
        dto.setLocation(room.getLocation());
        dto.setPrice(room.getPrice());           // বা pricePerNight যদি নাম অন্য হয়
        dto.setImagePath(room.getImagePath());
        dto.setDescription(room.getDescription());
        dto.setFacilities(room.getFacilities());
        // অন্যান্য ফিল্ড যা দরকার সেগুলো ম্যাপ করো
        // যেমন: dto.setAvailable(room.isAvailable());

        return dto;
    }


    private boolean isWishlisted = false;

    public boolean getIsWishlisted() { return isWishlisted; }
    public void setIsWishlisted(boolean isWishlisted) { this.isWishlisted = isWishlisted; }
}