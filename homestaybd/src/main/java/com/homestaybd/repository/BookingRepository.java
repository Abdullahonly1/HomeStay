package com.homestaybd.repository;

import com.homestaybd.model.Booking;
import com.homestaybd.model.BookingStatus;
import com.homestaybd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // এই মাসের আয়
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
            "WHERE b.room.host.id = :hostId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkInDate >= :startOfMonth")
    BigDecimal findEarningsThisMonth(@Param("hostId") Long hostId,
                                     @Param("startOfMonth") LocalDate startOfMonth);

    // আগামী চেক-ইন (আজ + কাল)
    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.room.host.id = :hostId " +
            "AND b.status = 'CONFIRMED' " +
            "AND b.checkInDate IN (:today, :tomorrow)")
    long countUpcomingCheckins(@Param("hostId") Long hostId,
                               @Param("today") LocalDate today,
                               @Param("tomorrow") LocalDate tomorrow);

    // পেন্ডিং বুকিং রিকোয়েস্ট
    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.room.host.id = :hostId " +
            "AND b.status = 'PENDING'")
    long countPendingBookingsForHost(@Param("hostId") Long hostId);

    // হোস্টের সব রিসিভড বুকিং
    @Query("SELECT b FROM Booking b WHERE b.room.host = :host")
    List<Booking> findByRoomHost(@Param("host") User host);

    // ইউজারের সব বুকিং
    List<Booking> findByUser(User user);



    // BookingRepository
    @Query("SELECT FUNCTION('DATE_FORMAT', b.checkInDate, '%Y-%m') AS month, SUM(b.totalPrice) " +
            "FROM Booking b " +
            "WHERE b.room.host.id = :hostId " +
            "AND b.status = :status " +
            "AND b.checkInDate BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE_FORMAT', b.checkInDate, '%Y-%m')")
    List<Object[]> getMonthlyEarningsSimple(
            @Param("hostId") Long hostId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("status") BookingStatus status
    );


    // আগামী বুকিং — Room এর host ফিল্ড ব্যবহার করে
    List<Booking> findByRoomHostAndStatusAndCheckInDateAfter(
            User host,
            BookingStatus status,
            LocalDate checkInDate
    );

    // সম্পন্ন বুকিং — Room এর host ফিল্ড ব্যবহার করে
    List<Booking> findByRoomHostAndStatusAndCheckOutDateBefore(
            User host,
            BookingStatus status,
            LocalDate checkOutDate
    );

    // Confirmed bookings (no date filter)
    List<Booking> findByRoomHostAndStatus(User host, BookingStatus status);


    // BookingRepository
    long countByRoomHostAndStatus(User host, BookingStatus status);
    long countByRoomHostAndStatusAndCheckInDateBetween(User host, BookingStatus status, LocalDate start, LocalDate end);
    List<Booking> findByRoomHostAndStatusAndCheckInDateBetween(User host, BookingStatus status, LocalDate start, LocalDate end);


    // Pending booking requests for host
    // ঠিক (তোমার Room-এ host আছে বলে)
    // এই লাইনটা যোগ করো
    //long countByRoomHostIdAndStatus(Long hostId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b JOIN b.room r WHERE r.host.id = :hostId AND b.status = :status")
    long countByRoomHostIdAndStatus(@Param("hostId") Long hostId, @Param("status") BookingStatus status);


    @Query("SELECT COALESCE(SUM(b.totalPrice), 0.0) FROM Booking b " +
            "JOIN b.room r " +
            "WHERE r.host.id = :ownerId " +                // ← owner → host
            "AND b.status = :status " +
            "AND b.checkInDate BETWEEN :start AND :end")
    Double sumTotalPriceByRoomHostIdAndStatusAndCheckInDateBetween(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    //long countByRoomHostIdAndStatusAndCheckInDateAfter(Long hostId, BookingStatus status, LocalDate date);


    @Query("SELECT COUNT(b) FROM Booking b JOIN b.room r WHERE r.host.id = :hostId AND b.status = :status AND b.checkInDate > :date")
    long countByRoomHostIdAndStatusAndCheckInDateAfter(
            Long hostId, BookingStatus status, LocalDate date
    );


    // এই মেথডটি অটো জেনারেট হবে যদি নাম ঠিক থাকে


    // অথবা যদি অটো না হয়, তাহলে @Query দিয়ে লিখতে পারো
    //@Query("SELECT COUNT(b) FROM Booking b WHERE b.room.owner.id = :ownerId AND b.status = :status")
    //long countByRoomOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status);



    List<Booking> findByRoomHostIdAndStatus(Long hostId, BookingStatus status);

    //long countByRoomHostIdAndStatus(Long hostId, BookingStatus status);




    long countByRoomHostAndStatusAndCheckInDateAfter(
            User host,
            BookingStatus status,
            LocalDate checkInDate
    );


    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.room.host = :host AND b.status = :status AND b.checkOutDate BETWEEN :start AND :end")
    Double sumTotalPriceByRoomHostAndStatusAndCheckOutDateBetween(
            @Param("host") User host,
            @Param("status") BookingStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );




}