package com.homestaybd.repository;

import com.homestaybd.model.Review;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * হোস্টের সব রুমের সব রিভিউ (room.host = host)
     */
    List<Review> findByRoomHost(User host);

    /**
     * একটা নির্দিষ্ট রুমের সব রিভিউ
     */
    List<Review> findByRoomId(Long roomId);

    /**
     * approved রিভিউ শুধু (optional)
     */
    List<Review> findByRoomHostAndApprovedTrue(User host);




    List<Review> findByRoom(Room room);
    List<Review> findByRoomAndApprovedTrue(Room room);
}