package com.homestaybd.service;

import com.homestaybd.dto.ChatRoomDTO;
import com.homestaybd.dto.MessageRequest;
import com.homestaybd.model.Message;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import com.homestaybd.repository.MessageRepository;
import com.homestaybd.repository.RoomRepository;
import com.homestaybd.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private UserRepository userRepository;

    /*public Message sendMessage(User sender, MessageRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("রুম পাওয়া যায়নি"));

        User receiver;

        if (sender.getId().equals(room.getHost().getId())) {
            // হোস্ট রিপ্লাই দিচ্ছে → শেষ মেসেজের sender-কে receiver করো
            Message lastMsg = messageRepository.findTopByRoomOrderByTimestampDesc(room);
            if (lastMsg != null && !lastMsg.getSender().getId().equals(sender.getId())) {
                receiver = lastMsg.getSender(); // গেস্ট
            } else {
                // যদি কোনো মেসেজ না থাকে বা শেষটা হোস্টের হয় — fallback
                receiver = room.getHost(); // অথবা exception দাও
                throw new RuntimeException("কোনো গেস্ট মেসেজ পাওয়া যায়নি");
            }
        } else {
            receiver = room.getHost();
        }

        Message message = new Message();
        message.setRoom(room);
        message.setSender(currentUser);
        message.setReceiver(null);          // ← এটা যোগ করো বা set না করো
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);

        return messageRepository.save(message);
    }*/




    public Message sendMessage(MessageRequest request, User sender) {   // ← sender প্যারামিটার যোগ করো
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Message message = new Message();
        message.setRoom(room);
        message.setSender(sender);
        message.setReceiver(null);           // optional receiver
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

   /* public List<Message> getMessagesForRoom(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("রুম পাওয়া যায়নি"));

        User otherUser = currentUser.getId().equals(room.getHost().getId())
                ? room.getHost()  // ভুল — হোস্ট হলে অন্য পার্টি হোস্ট নয়
                : room.getHost();

        // sender/receiver load করার জন্য Hibernate.initialize ব্যবহার করো
        List<Message> messages = messageRepository.findChatBetweenUserAndHost(
                roomId, currentUser.getId(), otherUser.getId());

        // sender ও receiver force load (lazy loading issue এড়াতে)
        messages.forEach(msg -> {
            Hibernate.initialize(msg.getSender());
            Hibernate.initialize(msg.getReceiver());
        });

        return messages;
    }*/

    public List<Message> getMessagesForRoom(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("রুম পাওয়া যায়নি"));

        boolean isHost = currentUser.getId().equals(room.getHost().getId());

        List<Message> messages;

        if (isHost) {
            // হোস্টের জন্য → রুমের সব মেসেজ (sender/receiver যাই হোক)
            messages = messageRepository.findByRoomIdOrderByTimestampAsc(roomId);
        } else {
            // গেস্টের জন্য → শুধু হোস্টের সাথে তার কথোপকথন
            User host = room.getHost();
            messages = messageRepository.findChatBetweenUsers(
                    roomId, currentUser.getId(), host.getId());
        }

        // Lazy loading issue এড়াতে sender/receiver initialize করা
        messages.forEach(msg -> {
            Hibernate.initialize(msg.getSender());
            Hibernate.initialize(msg.getReceiver());
        });

        return messages;
    }




    /**
     * হোস্টের সব চ্যাট রুম লিস্ট (যেখানে কোনো মেসেজ এসেছে)
     */
    public List<ChatRoomDTO> getHostChatRooms(User host) {
        // হোস্টের সব রুম খুঁজে বের করো
        List<Room> hostRooms = roomRepository.findByHost(host);

        List<ChatRoomDTO> chatRooms = new ArrayList<>();

        for (Room room : hostRooms) {
            // এই রুমে সব মেসেজ (হোস্ট receiver হিসেবে)
            List<Message> messages = messageRepository.findByRoomAndReceiverOrderByTimestampDesc(room, host);

            if (messages.isEmpty()) continue; // কোনো মেসেজ না থাকলে skip

            // লাস্ট মেসেজ
            Message lastMsg = messages.stream()
                    .max(Comparator.comparing(Message::getTimestamp))
                    .orElse(null);

            // unread কাউন্ট
            long unread = messages.stream()
                    .filter(m -> !m.isRead())
                    .count();

            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setRoomId(room.getId());
            dto.setRoomTitle(room.getTitle());
            dto.setLastMessage(lastMsg != null ? lastMsg.getContent().substring(0, Math.min(50, lastMsg.getContent().length())) + "..." : "");
            dto.setUnreadCount(unread);
            dto.setLastMessageTime(lastMsg != null ? lastMsg.getTimestamp() : null);

            chatRooms.add(dto);
        }

        // সর্ট করা — সবচেয়ে নতুন মেসেজ উপরে
        chatRooms.sort((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()));

        return chatRooms;
    }

    /**
     * হোস্টের মোট unread মেসেজ কাউন্ট (সব রুম মিলিয়ে)
     */
    public long getUnreadMessageCountForHost(User host) {
        return messageRepository.countUnreadMessagesForHost(host.getId());
    }
}
