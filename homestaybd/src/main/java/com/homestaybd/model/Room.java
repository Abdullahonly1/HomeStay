package com.homestaybd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String facilities;

    private String location;

    @Column(nullable = false)
    private double price;

    private String imagePath;

    private boolean available = true;

    // এই লাইনটা ঠিক রাখো — নাম হবে owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    //@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ← এটা যোগ করো
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "rooms"})  // ← rooms যদি User-এ back reference থাকে
    private User host;  // বা host যেটা ব্যবহার করছো


    @ElementCollection(fetch = FetchType.EAGER)   // ← EAGER করলে সহজে কাজ করে
    @CollectionTable(name = "room_additional_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_path")
    private List<String> additionalImages = new ArrayList<>();



    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public List<String> getAdditionalImages() { return additionalImages; }
    public void setAdditionalImages(List<String> additionalImages) { this.additionalImages = additionalImages; }
    // Lombok @Data দিয়ে getter/setter অটো হয়ে যাবে
    // তাই ম্যানুয়াল getOwner/setOwner লাগবে না


}
