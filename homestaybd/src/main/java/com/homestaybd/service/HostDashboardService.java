package com.homestaybd.service;

import com.homestaybd.dto.DashboardSummaryDTO;
import com.homestaybd.dto.EarningsDTO;
import com.homestaybd.dto.ReviewDTO;
import com.homestaybd.model.Booking;
import com.homestaybd.model.BookingStatus;
import com.homestaybd.model.Review;
import com.homestaybd.model.User;
import com.homestaybd.repository.BookingRepository;
import com.homestaybd.repository.MessageRepository;
import com.homestaybd.repository.ReviewRepository;
import com.homestaybd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class HostDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;


    // এই লাইনটা যোগ করো (যদি রিভিউ ফিচার চালু করতে চাও)
    @Autowired
    private ReviewRepository reviewRepository;

    // যদি MessageRepository না থাকে বা মেথড না থাকে — তাহলে এটা কমেন্ট করে রাখো
     @Autowired
     private MessageRepository messageRepository;

    public DashboardSummaryDTO getHostSummary(String username) {
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Host not found: " + username));

        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        // ১. এই মাসের আয় (CONFIRMED বুকিং থেকে)
        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        List<Booking> monthlyBookings = bookingRepository.findByRoomHostAndStatusAndCheckInDateBetween(
                host, BookingStatus.CONFIRMED, startOfMonth, endOfMonth);

        double thisMonthEarnings = monthlyBookings.stream()
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        summary.setEarningsThisMonth(thisMonthEarnings);  // ← null check সরানো হয়েছে

        // ২. আগামী চেক-ইন (আজ + কাল)
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        long upcomingCount = bookingRepository.countByRoomHostAndStatusAndCheckInDateBetween(
                host, BookingStatus.CONFIRMED, today, tomorrow);

        summary.setUpcomingCheckins((int) upcomingCount);

        // ৩. নতুন মেসেজ
        try {
            long unreadCount = messageRepository.countByReceiverAndIsReadFalse(host);
            summary.setUnreadMessages((int) unreadCount);
        } catch (Exception e) {
            System.out.println("Unread message count error: " + e.getMessage());
            summary.setUnreadMessages(0);
        }

        return summary;
    }


    public List<EarningsDTO> getSimpleEarnings(String username) {
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Host not found"));

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(5); // গত ৬ মাস (আজসহ)

        // ডাটাবেস থেকে মাস অনুযায়ী গ্রুপ করে আয় নেওয়া
        List<Object[]> results = bookingRepository.getMonthlyEarningsSimple(
                host.getId(),
                startDate,
                now,
                BookingStatus.CONFIRMED
        );

        // ৬ মাসের লিস্ট তৈরি (যদি কোনো মাসে আয় না থাকে তাহলে ০ দেখাবে)
        List<EarningsDTO> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            LocalDate current = startDate.plusMonths(i);
            String monthName = current.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + current.getYear();
            list.add(new EarningsDTO(monthName, 0));
        }

        // ডাটাবেস থেকে পাওয়া আয় বসানো
        for (Object[] row : results) {
            String monthKey = (String) row[0]; // যেমন "2026-01"
            double amount = ((Number) row[1]).doubleValue();

            for (EarningsDTO dto : list) {
                if (dto.getMonth().contains(monthKey)) {
                    dto.setAmount(amount);
                    break;
                }
            }
        }

        return list;
    }



    public List<ReviewDTO> getHostReviews(String username) {
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Host not found"));

        List<Review> reviews = reviewRepository.findByRoomHost(host);

        return reviews.stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setGuestUsername(review.getUser().getUsername());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setCreatedAt(review.getCreatedAt());
            dto.setRoomTitle(review.getRoom().getTitle());
            return dto;
        }).collect(Collectors.toList());
    }
}