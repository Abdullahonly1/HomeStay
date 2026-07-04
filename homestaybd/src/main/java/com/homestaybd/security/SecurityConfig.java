package com.homestaybd.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // Constructor injection (JwtFilter ইনজেক্ট করার জন্য)
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // পাবলিক স্ট্যাটিক পেজ + ফাইল
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/owner-dashboard.html",
                                "/user-dashboard.html",
                                "/my-trips.html",
                                "/wishlist.html",
                                "/room-details.html",
                                "/chat/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()

                        // JS, CSS, images, uploads – সব পাবলিক
                        .requestMatchers(
                                "/js/**",
                                "/css/**",
                                "/images/**",
                                "/uploads/**"
                        ).permitAll()

                        // Authentication endpoints – পাবলিক
                        .requestMatchers("/auth/**").permitAll()

                        // রুমের পাবলিক API (যদি দরকার হয়)
                        .requestMatchers("/api/rooms", "/api/rooms/**").permitAll()   // ← optional, public room list/details

                        // Host-specific protected endpoints
                        .requestMatchers(
                                "/api/rooms/my",           // আমার রুম লিস্ট
                                "/api/rooms/add",          // রুম অ্যাড
                                "/api/rooms/**"            // রুম আপডেট/ডিলিট
                        ).hasRole("OWNER")             // শুধু OWNER role-এর জন্য

                        // বাকি সব authenticated (যেকোনো লগইন ইউজার)
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


/*
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // ১. সব স্ট্যাটিক HTML পেজ পাবলিক (লগইন ছাড়াই খুলবে)
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/owner-dashboard.html",
                                "/user-dashboard.html",
                                "/room-details.html",
                                "/chat.html",
                                "/error"
                        ).permitAll()

                        // ২. সব স্ট্যাটিক ফাইল (CSS, JS, Images, Uploads, Favicon) পুরোপুরি পাবলিক
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ৩. অথেনটিকেশন সব API পাবলিক (লগইন, রেজিস্টার, me)
                        .requestMatchers("/auth/**").permitAll()

                        // ৪. পাবলিক রুম API (লগইন ছাড়া রুম লিস্ট দেখা যাবে)
                        .requestMatchers("/rooms", "/rooms/**").permitAll()

                        // ৫. বাকি সবকিছু লগইন দরকার (সিকিউর API)
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}*/