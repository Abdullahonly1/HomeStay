package com.homestaybd.controller;

import com.homestaybd.model.Booking;
import com.homestaybd.model.User;
import com.homestaybd.repository.BookingRepository;
import com.homestaybd.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CalendarController.java
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    @GetMapping("/bookings")
    public List<Booking> getHostBookings(Authentication auth) {
        String username = auth.getName();
        User host = userService.findByUsername(username);
        return bookingRepository.findByRoomHost(host);
    }
}