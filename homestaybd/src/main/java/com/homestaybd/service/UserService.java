package com.homestaybd.service;

import com.homestaybd.model.User;
import com.homestaybd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * ইউজারনেম দিয়ে ইউজার খুঁজে বের করা
     * RoomController এবং BookingService-এ ব্যবহার হবে
     */
    public User findByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("ইউজার পাওয়া যায়নি: " + username);
        }
        return userOptional.get();
    }

    /**
     * ইমেইল দিয়ে ইউজার খুঁজে বের করা (যদি দরকার হয়)
     */
    public User findByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("ইমেইল দিয়ে ইউজার পাওয়া যায়নি: " + email);
        }
        return userOptional.get();
    }

    /**
     * ইউজার সেভ করা (রেজিস্টারের সময় ব্যবহার হয়)
     */
    public User save(User user) {
        return userRepository.save(user);
    }
}
