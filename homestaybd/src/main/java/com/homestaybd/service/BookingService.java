package com.homestaybd.service;

import com.homestaybd.dto.BookingRequest;
import com.homestaybd.model.*;
import com.homestaybd.repository.BookingRepository;
import com.homestaybd.repository.RoomRepository;
import com.homestaybd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {


    // এই লাইনগুলো যোগ করো (যদি ইতিমধ্যে না থাকে)
    private final UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;


    @Autowired
    private UserService userService;
    @Autowired
    private RoomRepository roomRepository;

    // Constructor injection (সবচেয়ে ভালো প্র্যাকটিস)
    @Autowired
    public BookingService(
            UserRepository userRepository,
            BookingRepository bookingRepository
            // আরও repository যদি লাগে
    ) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    // createBooking মেথড (আগে থেকে থাকলে আপডেট করো)
    public Booking createBooking(BookingRequest request, User user) {
        // রুম খুঁজে বের করা
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("রুম পাওয়া যায়নি"));

        // রুম উপলব্ধ কি না চেক করা
        if (!room.isAvailable()) {
            throw new RuntimeException("রুম বর্তমানে উপলব্ধ নয়");
        }

        // তারিখ যাচাই
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new RuntimeException("চেক-আউট তারিখ চেক-ইনের পরে হতে হবে");
        }

        // দিন গণনা (রাতের সংখ্যা)
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new RuntimeException("অবৈধ তারিখের পরিসর");
        }

        // নতুন বুকিং তৈরি
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumberOfPersons(request.getNumberOfPersons());

        // মোট মূল্য হিসাব
        double totalPrice = room.getPrice() * nights * request.getNumberOfPersons();
        booking.setTotalPrice(totalPrice);

        // স্ট্যাটাস সেট (enum ব্যবহার করা হচ্ছে)
        booking.setStatus(BookingStatus.PENDING);

        // রুমকে অস্থায়ীভাবে unavailable করা (যাতে অন্য কেউ বুক না করতে পারে)
        room.setAvailable(false);
        roomRepository.save(room);  // রুম আপডেট

        // বুকিং সেভ
        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUser(user);
    }

    public List<Booking> getHostBookings(User host) {
        return bookingRepository.findByRoomHost(host);
    }

    // হোস্ট কনফার্ম করবে
    /*public Booking confirmBooking(Long bookingId, User host) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("বুকিং পাওয়া যায়নি"));

        // এখানে getOwner() এর বদলে getHost() করো
        if (!booking.getRoom().getHost().getId().equals(host.getId())) {
            throw new RuntimeException("এই বুকিং আপনার নয়");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("বুকিং আর পেন্ডিং নেই");
        }

        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }*/




    public Booking confirmBooking(Long bookingId, User host) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("বুকিং পাওয়া যায়নি"));

        // হোস্ট চেক
        if (!booking.getRoom().getHost().getId().equals(host.getId())) {
            throw new RuntimeException("এই বুকিং আপনার রুমের নয়");
        }

        // শুধু PENDING হলে কনফার্ম করা যাবে
        // এখানে enum-এর সাথে তুলনা করা হচ্ছে (String নয়)
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("বুকিং আর পেন্ডিং নেই। বর্তমান স্ট্যাটাস: " + booking.getStatus());
        }

        // ==================== পরিবর্তন এখানে ====================
        Room room = booking.getRoom();

        // যদি রুম available না থাকে তাহলে শুধু লগ করব, ব্লক করব না
        if (!room.isAvailable()) {
            System.out.println("সতর্কতা: রুম available = false ছিল, কিন্তু হোস্ট কনফার্ম করছে। ID = " + room.getId());
        }

        // কনফার্ম করা
        booking.setStatus(BookingStatus.CONFIRMED);

        // রুমকে বুকড করে দাও (যদি চাও — এখানে false রাখা হচ্ছে)
        room.setAvailable(false);
        roomRepository.save(room);

        Booking savedBooking = bookingRepository.save(booking);

        System.out.println("বুকিং সফলভাবে কনফার্ম হয়েছে। Booking ID = " + bookingId);
        return savedBooking;
    }

    // হোস্ট ক্যান্সেল করবে
    public Booking cancelBooking(Long bookingId, User host) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("বুকিং পাওয়া যায়নি"));

        // এখানে getOwner() এর বদলে getHost() করো
        if (!booking.getRoom().getHost().getId().equals(host.getId())) {
            throw new RuntimeException("এই বুকিং আপনার নয়");
        }

        // এখন (ঠিক):
        booking.setStatus(BookingStatus.CANCELLED);

        // রুম আবার উপলব্ধ করা
        Room room = booking.getRoom();
        room.setAvailable(true);
        roomRepository.save(room);

        return bookingRepository.save(booking);
    }

    public List<Booking> getUpcomingBookingsForHost(String username) {
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Host not found"));

        LocalDate today = LocalDate.now();

        // এখানে পরিবর্তন করো
        return bookingRepository.findByRoomHostAndStatusAndCheckInDateAfter(
                host, BookingStatus.CONFIRMED, today
        );
    }

    public List<Booking> getCompletedBookingsForHost(String username) {
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Host not found"));

        LocalDate today = LocalDate.now();

        // এখানে পরিবর্তন করো
        return bookingRepository.findByRoomHostAndStatusAndCheckOutDateBefore(
                host, BookingStatus.CONFIRMED, today
        );
    }


    public List<Booking> getConfirmedBookingsForHost(String username) {
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Host not found"));

        return bookingRepository.findByRoomHostAndStatus(host, BookingStatus.CONFIRMED);
    }


    public Long countPendingByOwner(Long hostId) {
        return bookingRepository.countByRoomHostIdAndStatus(hostId, BookingStatus.PENDING);
    }


    /**
     * হোস্টের পেন্ডিং বুকিংগুলো রিটার্ন করবে (status = PENDING)
     */
    public List<Booking> getPendingBookingsForHost(String username) {
        User host = userService.findByUsername(username);
        if (host == null || host.getRole() != Role.OWNER) {
            throw new IllegalArgumentException("Invalid host");
        }

        return bookingRepository.findByRoomHostIdAndStatus(host.getId(), BookingStatus.PENDING);
    }

    // যদি তুমি চাও শুধু count করতে (ড্যাশবোর্ডের জন্য)
    public Long countPendingBookingsForHost(String username) {
        User host = userService.findByUsername(username);
        if (host == null || host.getRole() != Role.OWNER) {
            return 0L;
        }
        return bookingRepository.countByRoomHostIdAndStatus(host.getId(), BookingStatus.PENDING);
    }


    public long getPendingBookingCountForHost(Long hostId) {
        return bookingRepository.countByRoomHostIdAndStatus(hostId, BookingStatus.PENDING);
    }



}