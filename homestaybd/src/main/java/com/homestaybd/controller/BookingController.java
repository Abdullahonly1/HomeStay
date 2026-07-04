package com.homestaybd.controller;

import com.homestaybd.dto.BookingRequest;
import com.homestaybd.dto.NotificationDTO;
import com.homestaybd.model.Booking;
import com.homestaybd.model.BookingStatus;
import com.homestaybd.model.Role;
import com.homestaybd.model.User;
import com.homestaybd.repository.BookingRepository;
import com.homestaybd.repository.UserRepository;
import com.homestaybd.service.BookingService;
import com.homestaybd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")  // /api/ prefix যোগ করা — frontend-এর সাথে মিলবে
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000", "*"})  // টেস্টের জন্য *
public class BookingController {

    @Autowired
    private BookingService bookingService;


    @Autowired
    private UserService userService;


    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // ১. ইউজার নতুন বুকিং করবে
    @PostMapping  // তোমার URL অনুযায়ী পাথ দাও
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        if (user.getRole() == Role.OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Owners cannot make bookings");
        }

        try {
            // বুকিং তৈরি করা
            Booking booking = bookingService.createBooking(request, user);

            // ------------------ WebSocket Notification পাঠানো ------------------

            // হোস্টকে (রুমের host) নোটিফাই করা
            User host = booking.getRoom().getHost();   // ← এখানে getHost() ব্যবহার করা হচ্ছে

            if (host != null && booking.getStatus() == BookingStatus.PENDING) {
                // 1. নতুন বুকিং নোটিফিকেশন
                NotificationDTO notification = new NotificationDTO();
                notification.setType("NEW_BOOKING");
                notification.setMessage("নতুন বুকিং রিকোয়েস্ট এসেছে");
                notification.setGuestUsername(user.getUsername());
                notification.setRoomId(booking.getRoom().getId());
                notification.setBookingId(booking.getId());
                notification.setTimestamp(booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now());

                messagingTemplate.convertAndSendToUser(
                        host.getId().toString(),
                        "/queue/notifications",
                        notification
                );

                // 2. হোস্টের পেন্ডিং কাউন্ট আপডেট করে পাঠানো
                long pendingCount = bookingService.getPendingBookingCountForHost(host.getId());

                messagingTemplate.convertAndSendToUser(
                        host.getId().toString(),
                        "/queue/pending-count",
                        pendingCount
                );
            }

            // ------------------ WebSocket শেষ ------------------

            return ResponseEntity.ok(booking);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create booking: " + e.getMessage());
        }
    }

    // ২. ইউজার তার নিজের সব বুকিং দেখবে
    @GetMapping("/my")
    public ResponseEntity<List<Booking>> getMyBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = auth.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<Booking> bookings = bookingService.getUserBookings(user);
        return ResponseEntity.ok(bookings);
    }

    // ৩. হোস্ট তার রিসিভড বুকিং দেখবে (সব status)
    @GetMapping("/received")
    public ResponseEntity<List<Booking>> getReceivedBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = auth.getName();
        System.out.println("হোস্ট API কল হয়েছে। ইউজারনেম: " + username);

        User host = userService.findByUsername(username);

        if (host == null) {
            System.out.println("হোস্ট পাওয়া যায়নি: " + username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (host.getRole() != Role.OWNER) {
            System.out.println("ইউজার OWNER নয়। Role: " + host.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<Booking> bookings = bookingService.getHostBookings(host);
        System.out.println("হোস্টের মোট বুকিং সংখ্যা: " + bookings.size());
        return ResponseEntity.ok(bookings);
    }

    // ৪. হোস্ট বুকিং কনফার্ম করবে
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        String username = auth.getName();
        System.out.println("কনফার্ম API কল: বুকিং ID = " + id + " | হোস্ট: " + username);

        User host = userService.findByUsername(username);

        if (host == null || host.getRole() != Role.OWNER) {
            System.out.println("অনুমতি নেই বা হোস্ট পাওয়া যায়নি। Role: " + (host != null ? host.getRole() : "null"));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only owners can confirm bookings");
        }

        try {
            Booking booking = bookingService.confirmBooking(id, host);
            System.out.println("বুকিং সফলভাবে কনফার্ম হয়েছে। Booking ID = " + id);
            return ResponseEntity.ok(booking);
        } catch (IllegalStateException e) {
            System.out.println("কনফার্ম করতে পারা যায়নি: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("কনফার্ম করার সময় অপ্রত্যাশিত ত্রুটি: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }

    // ৫. হোস্ট বুকিং ক্যান্সেল করবে
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        String username = auth.getName();
        System.out.println("ক্যান্সেল API কল: বুকিং ID = " + id + " | হোস্ট: " + username);

        User host = userService.findByUsername(username);

        if (host == null || host.getRole() != Role.OWNER) {
            System.out.println("অনুমতি নেই। Role: " + (host != null ? host.getRole() : "null"));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only owners can cancel bookings");
        }

        try {
            Booking booking = bookingService.cancelBooking(id, host);
            System.out.println("বুকিং ক্যান্সেল সফল: ID = " + id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            System.out.println("ক্যান্সেল এরর: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ৬. হোস্টের কনফার্মড বুকিং (আগের + নতুন)
    @GetMapping("/confirmed")
    public ResponseEntity<List<Booking>> getConfirmedBookings(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = principal.getName();
        User host = userService.findByUsername(username);

        if (host == null || host.getRole() != Role.OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<Booking> confirmed = bookingService.getConfirmedBookingsForHost(username);
        return ResponseEntity.ok(confirmed);
    }

    // ৭. আগামী বুকিং (চেক-ইন ভবিষ্যতে + confirmed)
    @GetMapping("/upcoming")
    public ResponseEntity<List<Booking>> getUpcomingBookings(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = principal.getName();
        List<Booking> upcoming = bookingService.getUpcomingBookingsForHost(username);
        return ResponseEntity.ok(upcoming);
    }

    // ৮. সম্পন্ন বুকিং
    @GetMapping("/completed")
    public ResponseEntity<List<Booking>> getCompletedBookings(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = principal.getName();
        List<Booking> completed = bookingService.getCompletedBookingsForHost(username);
        return ResponseEntity.ok(completed);
    }

    // Optional: পেন্ডিং বুকিং (হোস্টের জন্য)
    @GetMapping("/pending")
    public ResponseEntity<List<Booking>> getPendingBookings(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = principal.getName();
        System.out.println("Pending request for user: " + username);

        User host = userService.findByUsername(username);
        if (host == null) {
            System.out.println("Host not found for username: " + username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (host.getRole() != Role.OWNER) {
            System.out.println("User is not OWNER: " + username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        System.out.println("Host ID: " + host.getId()); // ← sorna-এর ID 1 হওয়া উচিত

        List<Booking> pending = bookingService.getPendingBookingsForHost(username);

        System.out.println("Pending bookings count: " + pending.size());
        if (!pending.isEmpty()) {
            System.out.println("First pending booking ID: " + pending.get(0).getId());
        }

        return ResponseEntity.ok(pending);
    }



    /**
     * হোস্টের জন্য পেন্ডিং (অপেক্ষমান) বুকিং সংখ্যা রিটার্ন করে
     * frontend-এ badge / notification-এর জন্য ব্যবহার হয়
     */
    // Pending count endpoint
    @GetMapping("/pending-count")
    public ResponseEntity<Long> getPendingCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(0L);
        }

        String username = authentication.getName();
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        long pendingCount = bookingRepository.countByRoomHostAndStatus(host, BookingStatus.PENDING);

        return ResponseEntity.ok(pendingCount);
    }

    /**
     * হোস্টের জন্য আসন্ন (upcoming) বুকিং সংখ্যা রিটার্ন করে
     * আজকের পরের দিন থেকে শুরু হওয়া CONFIRMED বুকিং গণনা করে
     */
    // Upcoming count endpoint
    @GetMapping("/upcoming-count")
    public ResponseEntity<Long> getUpcomingCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(0L);
        }

        String username = authentication.getName();
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        LocalDate today = LocalDate.now();

        long upcomingCount = bookingRepository.countByRoomHostAndStatusAndCheckInDateAfter(
                host,
                BookingStatus.CONFIRMED,
                today
        );

        return ResponseEntity.ok(upcomingCount);
    }



    // EarningsController.java বা BookingController-এ
    @GetMapping("/earnings/this-month")
    public ResponseEntity<Double> getThisMonthEarnings(Authentication auth) {
        String username = auth.getName();
        User host = userRepository.findByUsername(username).orElseThrow();

        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1);

        Double earnings = bookingRepository.sumTotalPriceByRoomHostAndStatusAndCheckOutDateBetween(
                host, BookingStatus.COMPLETED, start, end
        );

        return ResponseEntity.ok(earnings != null ? earnings : 0.0);
    }

}