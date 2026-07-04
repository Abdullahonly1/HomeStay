package com.homestaybd.controller;

import com.homestaybd.dto.ChatRoomDTO;
import com.homestaybd.dto.MessageDTO;
import com.homestaybd.dto.MessageRequest;
import com.homestaybd.dto.NotificationDTO;
import com.homestaybd.model.Message;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import com.homestaybd.repository.MessageRepository;
import com.homestaybd.repository.RoomRepository;
import com.homestaybd.repository.UserRepository;
import com.homestaybd.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
//@RequestMapping("/chat")
@RequestMapping("/api/chat")   // ← এটা থাকতে হবে
public class ChatController {

    private final ChatService chatService;
    private final MessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(
            ChatService chatService,
            MessageRepository messageRepository,
            RoomRepository roomRepository,
            UserRepository userRepository) {
        this.chatService = chatService;
        this.messageRepository = messageRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    /**
     * মেসেজ পাঠানো (যেকোনো ইউজার → রুমের হোস্ট)
     */
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestBody MessageRequest request,
            Authentication authentication) {

        // বর্তমান লগইন ইউজার (sender)
        String username = authentication.getName();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // রুম খুঁজে বের করা
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        // receiver হবে রুমের host
        User receiver = room.getHost();   // ← এখানে getHost() ব্যবহার করা হচ্ছে
        if (receiver == null) {
            throw new RuntimeException("Room has no host assigned");
        }

        // Message entity তৈরি
        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setRoom(room);
        message.setIsRead(false);
        message.setTimestamp(LocalDateTime.now());

        // ডাটাবেসে সেভ
        Message savedMessage = messageRepository.save(message);

        // ------------------ WebSocket Notification পাঠানো ------------------

        // 1. নতুন মেসেজের নোটিফিকেশন (host-এর জন্য)
        NotificationDTO notification = new NotificationDTO();
        notification.setType("NEW_MESSAGE");
        notification.setMessage("একটি নতুন মেসেজ এসেছে");
        notification.setSenderUsername(sender.getUsername());
        notification.setRoomId(room.getId());
        notification.setTimestamp(savedMessage.getTimestamp());

        messagingTemplate.convertAndSendToUser(
                receiver.getId().toString(),
                "/queue/notifications",
                notification
        );

        // 2. host-এর unread message count আপডেট করে পাঠানো
        long unreadCount = messageRepository.countByReceiverAndIsReadFalse(receiver);

        messagingTemplate.convertAndSendToUser(
                receiver.getId().toString(),
                "/queue/unread-count",
                unreadCount
        );

        // ------------------ WebSocket শেষ ------------------

        // DTO তৈরি করে রিটার্ন
        MessageDTO dto = new MessageDTO();
        dto.setId(savedMessage.getId());
        dto.setContent(savedMessage.getContent());
        dto.setIsRead(savedMessage.isRead());
        dto.setSenderId(savedMessage.getSender().getId());
        dto.setSenderUsername(savedMessage.getSender().getUsername());
        dto.setReceiverId(savedMessage.getReceiver().getId());
        dto.setRoomId(savedMessage.getRoom().getId());
        dto.setTimestamp(savedMessage.getTimestamp());

        return ResponseEntity.ok(dto);
    }

    /**
     * একটা নির্দিষ্ট রুমের সব মেসেজ দেখা (chronological order-এ)
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<MessageDTO>> getRoomChat(
            @PathVariable Long roomId,
            Authentication authentication) {

        // অথেনটিকেশন চেক (optional – যদি চাও শুধু participant-রাই দেখতে পারে)
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // রুম খুঁজে বের করা
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // শুধুমাত্র রুমের মেসেজগুলো নেওয়া
        List<Message> messages = messageRepository.findByRoomIdOrderByTimestampAsc(roomId);

        // DTO-তে কনভার্ট
        List<MessageDTO> dtos = messages.stream().map(msg -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(msg.getId());
            dto.setContent(msg.getContent());
            dto.setIsRead(msg.isRead());
            dto.setSenderId(msg.getSender().getId());
            dto.setSenderUsername(msg.getSender().getUsername());
            dto.setReceiverId(msg.getReceiver().getId());
            dto.setRoomId(msg.getRoom().getId());
            dto.setTimestamp(msg.getTimestamp());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * হোস্টের জন্য সব চ্যাট রুম (যেসব রুমে কেউ মেসেজ পাঠিয়েছে)
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getHostChatRooms(Authentication authentication) {
        String username = authentication.getName();
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // হোস্টের সব রুম যেগুলোতে কোনো মেসেজ আছে
        List<Room> rooms = roomRepository.findByHost(host);

        List<ChatRoomDTO> chatRooms = rooms.stream()
                .filter(room -> !messageRepository.findByRoomIdOrderByTimestampAsc(room.getId()).isEmpty())
                .map(room -> {
                    ChatRoomDTO dto = new ChatRoomDTO();
                    dto.setRoomId(room.getId());
                    dto.setRoomTitle(room.getTitle());
                    dto.setRoomImage(room.getImagePath());
                    // সর্বশেষ মেসেজ (optional)
                    List<Message> msgs = messageRepository.findByRoomIdOrderByTimestampAsc(room.getId());
                    if (!msgs.isEmpty()) {
                        Message last = msgs.get(msgs.size() - 1);
                        dto.setLastMessage(last.getContent());
                        dto.setLastMessageTime(last.getTimestamp());
                    }
                    // unread count (optional – যদি চাও)
                    long unread = messageRepository.countByRoomAndReceiverAndIsReadFalse(room, host);
                    dto.setUnreadCount(unread);
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * হোস্টের জন্য unread মেসেজের সংখ্যা
     */


    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        long count = messageRepository.countByReceiverAndIsReadFalse(user);
        return ResponseEntity.ok(count);
    }
}