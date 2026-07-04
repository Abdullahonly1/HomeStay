package com.homestaybd.repository;

import com.homestaybd.model.Message;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * একটা রুমের সব মেসেজ টাইমস্ট্যাম্প অনুসারে সাজিয়ে লোড করা
     * (হোস্টের চ্যাটবক্সে পুরো কথোপকথন দেখানোর জন্য ব্যবহার করা হয়)
     */
    List<Message> findByRoomIdOrderByTimestampAsc(Long roomId);

    /**
     * একটা নির্দিষ্ট রুমে দুইজন ইউজারের মধ্যে সব চ্যাট মেসেজ
     * (গেস্ট/ইউজারের জন্য ব্যবহার হয় — শুধু হোস্টের সাথে তার কথোপকথন)
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.room.id = :roomId " +
            "AND ((m.sender.id = :userId AND m.receiver.id = :otherId) " +
            "  OR (m.sender.id = :otherId AND m.receiver.id = :userId)) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findChatBetweenUsers(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("otherId") Long otherId);

    /**
     * হোস্টের জন্য সব রুম মিলিয়ে মোট unread মেসেজ কাউন্ট
     * (নোটিফিকেশন ব্যাজ / unread count দেখানোর জন্য)
     */
    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE m.room.host.id = :hostId " +           // ← owner → host
            "AND m.isRead = false " +
            "AND m.sender.id != :hostId")
    long countUnreadMessagesForHost(@Param("hostId") Long hostId);

    /**
     * একটা নির্দিষ্ট রুমে হোস্ট যে receiver — সব মেসেজ
     * (হোস্ট ড্যাশবোর্ডে চ্যাট লিস্ট / last message দেখানোর জন্য)
     */
    List<Message> findByRoomAndReceiverOrderByTimestampDesc(Room room, User receiver);

    /**
     * একটা রুমের সবচেয়ে নতুন মেসেজ (last message) পাওয়ার জন্য
     * (চ্যাট রুম লিস্টে last message preview দেখানোর জন্য)
     */
    Message findTopByRoomOrderByTimestampDesc(Room room);

    //long countByReceiverAndIsReadFalse(User receiver);

    // Unread messages count
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    long countByReceiverAndIsReadFalse(User host);


    // হোস্টের জন্য unread count (একটা রুমে)
    long countByRoomAndReceiverAndIsReadFalse(Room room, User receiver);

    // হোস্টের মোট unread (সব রুম মিলিয়ে)

}