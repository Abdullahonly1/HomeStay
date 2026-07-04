package com.homestaybd.controller;

import com.homestaybd.dto.DashboardSummaryDTO;
import com.homestaybd.dto.EarningsDTO;
import com.homestaybd.dto.ReviewDTO;
import com.homestaybd.model.BookingStatus;
import com.homestaybd.model.User;
import com.homestaybd.repository.BookingRepository;
import com.homestaybd.repository.MessageRepository;
import com.homestaybd.service.HostDashboardService;
import com.homestaybd.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
public class DashboardController {

    private final UserService userService;
    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final HostDashboardService hostDashboardService;

    @Autowired
    public DashboardController(
            UserService userService,
            MessageRepository messageRepository,
            BookingRepository bookingRepository,
            HostDashboardService hostDashboardService) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.bookingRepository = bookingRepository;
        this.hostDashboardService = hostDashboardService;
    }

    @GetMapping("/host-summary")
    public ResponseEntity<DashboardSummaryDTO> getHostSummary(Principal principal) {
        String username = principal.getName();
        User owner = userService.findByUsername(username);

        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        // পেন্ডিং অনুরোধ
        long pending = bookingRepository.countByRoomHostIdAndStatus(owner.getId(), BookingStatus.PENDING);
        summary.setPendingRequests((int) pending);

        // নতুন মেসেজ
        long unread = messageRepository.countByReceiverIdAndIsReadFalse(owner.getId());
        summary.setUnreadMessages((int) unread);

        // এই মাসের আয় (এখানে ভ্যারিয়েবল ডিক্লেয়ার + ক্যালকুলেট)
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);

        Double thisMonthEarnings = bookingRepository.sumTotalPriceByRoomHostIdAndStatusAndCheckInDateBetween(
                owner.getId(),
                BookingStatus.CONFIRMED,
                firstDayOfMonth,
                lastDayOfMonth
        );

        // এখানে setter কল
        summary.setEarningsThisMonth(thisMonthEarnings != null ? thisMonthEarnings : 0.0);

        // আগামী চেক-ইন (যদি থাকে)
        long upcoming = bookingRepository.countByRoomHostIdAndStatusAndCheckInDateAfter(
                owner.getId(),
                BookingStatus.CONFIRMED,
                LocalDate.now()
        );
        summary.setUpcomingCheckins((int) upcoming);

        log.info("Host {}: earningsThisMonth = {}, pending = {}",
                owner.getUsername(), thisMonthEarnings, pending);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/host-earnings-simple")
    public ResponseEntity<List<EarningsDTO>> getSimpleEarnings(Principal principal) {
        String username = principal.getName();
        List<EarningsDTO> earnings = hostDashboardService.getSimpleEarnings(username);
        return ResponseEntity.ok(earnings);
    }

    @GetMapping("/host-reviews")
    public ResponseEntity<List<ReviewDTO>> getHostReviews(Principal principal) {
        String username = principal.getName();
        List<ReviewDTO> reviews = hostDashboardService.getHostReviews(username);
        return ResponseEntity.ok(reviews);
    }
}