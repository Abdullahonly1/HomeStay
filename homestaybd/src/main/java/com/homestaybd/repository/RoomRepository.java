package com.homestaybd.repository;

import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // সবচেয়ে সহজ ও প্রস্তাবিত — User অবজেক্ট দিয়ে
    List<Room> findByHost(User host);

    // শুধু hostId (Long) দিয়ে খুঁজতে চাইলে — এটাও কাজ করবে
    List<Room> findByHostId(Long hostId);

    // কাস্টম কোয়েরি (যদি উপরের দুটো কোনো কারণে কাজ না করে)
    @Query("SELECT r FROM Room r WHERE r.host.id = :hostId")
    List<Room> findRoomsByHostId(@Param("hostId") Long hostId);

    // তোমার আগের অন্যান্য মেথড (যেমন available rooms)
    List<Room> findByAvailableTrue();

    List<Room> findByLocationContainingIgnoreCaseAndAvailableTrue(String location);


    // লোকেশন partial match (ignore case)
    List<Room> findByLocationContainingIgnoreCase(String location);

    long countByHostId(Long hostId);


    @EntityGraph(attributePaths = { "additionalImages" })
    Optional<Room> findById(Long id);




}