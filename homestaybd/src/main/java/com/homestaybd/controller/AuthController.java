package com.homestaybd.controller;

import com.homestaybd.dto.LoginRequest;
import com.homestaybd.dto.RegisterRequest;
import com.homestaybd.model.User;
import com.homestaybd.repository.UserRepository;
import com.homestaybd.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            String message = authService.register(request);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("রেজিস্টার ফেল হয়েছে: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            e.printStackTrace(); // লগে দেখার জন্য
            return ResponseEntity.status(401).body("লগইন ফেল হয়েছে: " + e.getMessage());
        }
    }

    // একটা মাত্র /me এন্ডপয়েন্ট — এটা সবসময় কাজ করবে
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        // SecurityContext থেকে authentication নেওয়া হচ্ছে
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build(); // লগইন না থাকলে 401
        }

        // username নিয়ে ডাটাবেস থেকে ইউজার লোড করা হচ্ছে
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("ইউজার পাওয়া যায়নি"));

        return ResponseEntity.ok(user);
    }
}