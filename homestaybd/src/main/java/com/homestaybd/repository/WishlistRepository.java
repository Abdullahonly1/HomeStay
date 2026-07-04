/*ackage com.homestaybd.repository;

import com.homestaybd.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    void deleteByUserIdAndRoomId(Long userId, Long roomId);
    List<Long> findRoomIdsByUserId(Long userId);
}*/

package com.homestaybd.repository;

import com.homestaybd.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // ইউজার + রুম কম্বিনেশন দিয়ে ডিলিট
    void deleteByUserIdAndRoomId(Long userId, Long roomId);

    // শুধু roomId গুলো নেওয়ার জন্য সঠিক JPQL
    @Query("SELECT w.roomId FROM Wishlist w WHERE w.userId = :userId")
    List<Long> findRoomIdsByUserId(@Param("userId") Long userId);

    // অপশনাল: চেক করতে যে এই রুম ইতিমধ্যে wishlist-এ আছে কি না
    boolean existsByUserIdAndRoomId(Long userId, Long roomId);
}
