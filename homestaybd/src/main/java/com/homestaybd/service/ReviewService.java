package com.homestaybd.service;

import com.homestaybd.model.Review;
import com.homestaybd.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    // আরও ফাংশন চাইলে যোগ করতে পারো, যেমন:
    // public List<Review> getReviewsByRoomId(Long roomId) { ... }
}