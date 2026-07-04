package com.homestaybd.service;

import com.homestaybd.dto.LoginRequest;
import com.homestaybd.dto.RegisterRequest;
import com.homestaybd.model.Role;
import com.homestaybd.model.User;
import com.homestaybd.repository.UserRepository;
import com.homestaybd.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent() ||
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userRepository.save(user);
        return "User registered";
    }

    public String login(LoginRequest request) {
        System.out.println("Login request received for username: " + request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            System.out.println("Authentication successful for: " + request.getUsername());

            String token = jwtUtils.generateToken(authentication);
            System.out.println("Token generated successfully");

            return token;
        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials for username: " + request.getUsername());
            throw new RuntimeException("ভুল ইউজারনেম বা পাসওয়ার্ড");
        } catch (UsernameNotFoundException e) {
            System.out.println("User not found: " + request.getUsername());
            throw new RuntimeException("ইউজার পাওয়া যায়নি");
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("লগইন করতে সমস্যা হয়েছে: " + e.getMessage());
        }
    }
}
