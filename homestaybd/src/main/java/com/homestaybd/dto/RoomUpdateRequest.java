package com.homestaybd.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RoomUpdateRequest {
    private String title;
    private String description;
    private String location;
    private Double pricePerNight;  // price_per_night বা price যাই হোক
    private String facilities;
    // MultipartFile image টা কন্ট্রোলারে আলাদা @RequestParam হিসেবে নিবে, এখানে রাখার দরকার নেই
}