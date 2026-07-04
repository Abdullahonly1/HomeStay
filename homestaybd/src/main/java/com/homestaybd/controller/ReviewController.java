package com.homestaybd.controller;

import com.homestaybd.dto.ReviewDTO;
import com.homestaybd.model.Review;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import com.homestaybd.repository.ReviewRepository;
import com.homestaybd.repository.RoomRepository;
import com.homestaybd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    // গেস্ট রিভিউ দিচ্ছে (ইতিমধ্যে কাজ করছে)
    @PostMapping
    public ResponseEntity<String> addReview(@RequestBody ReviewDTO reviewDTO, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findById(reviewDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Review review = new Review();
        review.setUser(user);
        review.setRoom(room);
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setApproved(false); // অ্যাডমিন/হোস্ট অ্যাপ্রুভ করবে

        reviewRepository.save(review);

        return ResponseEntity.ok("Review added successfully");
    }

    // হোস্টের সব রুমের সব রিভিউ (যেগুলো approved বা pending)
    @GetMapping("/my")
    public ResponseEntity<List<ReviewDTO>> getMyReviews(Authentication authentication) {
        String username = authentication.getName();
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // হোস্টের সব রুম খুঁজে বের করা (Room.java-তে host ফিল্ড আছে)
        List<Room> myRooms = roomRepository.findByHost(host);

        // সব রুমের রিভিউ কালেক্ট করা
        List<Review> reviews = myRooms.stream()
                .flatMap(room -> reviewRepository.findByRoom(room).stream())
                .collect(Collectors.toList());

        List<ReviewDTO> dtos = reviews.stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setRoomId(review.getRoom().getId());
            dto.setRoomTitle(review.getRoom().getTitle());
            dto.setUserId(review.getUser().getId());
            dto.setUsername(review.getUser().getUsername());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setCreatedAt(review.getCreatedAt());
            dto.setApproved(review.isApproved());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // একটা রুমের সব রিভিউ (publicly visible, যেমন room detail page-এ)
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByRoom(@PathVariable Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<Review> reviews = reviewRepository.findByRoomAndApprovedTrue(room);

        List<ReviewDTO> dtos = reviews.stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(review.getId());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setUsername(review.getUser().getUsername());
            dto.setCreatedAt(review.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}