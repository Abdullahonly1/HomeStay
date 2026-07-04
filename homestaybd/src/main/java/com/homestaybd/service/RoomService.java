package com.homestaybd.service;

import com.homestaybd.dto.RoomRequest;
import com.homestaybd.dto.RoomUpdateRequest;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import com.homestaybd.repository.RoomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    // ইমেজ সেভ করার ফোল্ডার (তোমার দেওয়া পাথ রাখলাম)
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/rooms/";

    // Constructor injection — @Autowired ছাড়াই কাজ করবে
    public RoomService(RoomRepository roomRepository, UserService userService) {
        this.roomRepository = roomRepository;
        this.userService = userService;
    }

    @Transactional
    public Room saveRoom(Room room) {
        logger.info("রুম সেভ হচ্ছে → Title: {}", room.getTitle());
        return roomRepository.save(room);
    }

    // হোস্টের রুম লোড — duplicate মেথড ডিলিট করা হয়েছে, একটাই রাখলাম
    public List<Room> getRoomsByHost(Long hostId) {
        logger.info("হোস্টের রুম লোড হচ্ছে → Host ID: {}", hostId);
        return roomRepository.findByHostId(hostId);
    }

    // সব উপলব্ধ রুম
    public List<Room> findAllAvailable() {
        return roomRepository.findByAvailableTrue();
    }

    // লোকেশন দিয়ে সার্চ
    public List<Room> findByLocationContainingIgnoreCase(String location) {
        return roomRepository.findByLocationContainingIgnoreCase(location);
    }

    @Transactional
    public Room addRoom(RoomRequest request, MultipartFile image, User host) throws IOException {
        Room room = new Room();

        room.setHost(host);
        room.setTitle(request.getTitle());
        room.setDescription(request.getDescription());
        room.setPrice(request.getPrice());
        room.setFacilities(request.getFacilities());
        room.setLocation(request.getLocation());
        room.setAvailable(true);

        String imagePath = "/images/placeholder-room.jpg";

        // ইমেজ আপলোড লজিক (তোমার কোড একদম একই রাখলাম)
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(UPLOAD_DIR + fileName);

                Files.createDirectories(uploadPath.getParent());
                Files.copy(image.getInputStream(), uploadPath);

                imagePath = "/uploads/rooms/" + fileName;

                logger.info("✅ ইমেজ সেভ সফল: {}", uploadPath.toAbsolutePath());
            } catch (Exception e) {
                logger.error("❌ ইমেজ সেভে সমস্যা: {}", e.getMessage());
            }
        }

        room.setImagePath(imagePath);

        // Flush + Clear (তোমার কোড রাখলাম)
        Room savedRoom = roomRepository.saveAndFlush(room);
        entityManager.flush();
        entityManager.clear();

        return savedRoom;
    }

    /**
     * সব উপলব্ধ রুম
     */
    public List<Room> getAllAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    /**
     * হোস্টের নিজের রুম লিস্ট
     */
    public List<Room> getMyRooms(User host) {
        // Authentication থেকে current user নেওয়া (তোমার কোড রাখলাম)
        // String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // User currentUser = userService.findByUsername(username);

        // সরাসরি host পাস করা হচ্ছে — তাই এটা সিম্পল রাখলাম
        return roomRepository.findByHostId(host.getId());
    }

    /**
     * রুম আপডেট করা (তোমার পুরো লজিক রাখলাম + logger যোগ করলাম)
     */
    @Transactional
    public Room updateRoom(Long roomId, RoomUpdateRequest request, MultipartFile image, User host) throws IOException {
        // ১. রুম খুঁজে বের করা
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("রুম ID " + roomId + " পাওয়া যায়নি"));

        // ২. চেক করা যে এই রুমটা এই হোস্টের কি না
        if (!room.getHost().getId().equals(host.getId())) {
            throw new RuntimeException("এই রুম আপনার নয়। আপনার ID: " + host.getId() +
                    ", রুমের Host ID: " + room.getHost().getId());
        }

        // ৩. নতুন ডাটা দিয়ে আপডেট করা (null-safe + blank চেক)
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            room.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            room.setDescription(request.getDescription().trim());
        }
        if (request.getPricePerNight() != null && request.getPricePerNight() > 0) {
            room.setPrice(request.getPricePerNight());
        }
        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            room.setLocation(request.getLocation().trim());
        }
        if (request.getFacilities() != null && !request.getFacilities().trim().isEmpty()) {
            room.setFacilities(request.getFacilities().trim());
        }

        // ৪. নতুন ইমেজ থাকলে আপলোড + পুরানো ডিলিট
        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "uploads/rooms/";
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // পুরানো ইমেজ ডিলিট
                if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
                    String oldFileName = room.getImagePath().substring(room.getImagePath().lastIndexOf("/") + 1);
                    Path oldFilePath = uploadPath.resolve(oldFileName);
                    try {
                        Files.deleteIfExists(oldFilePath);
                        logger.info("পুরানো ইমেজ ডিলিট হয়েছে: {}", oldFilePath);
                    } catch (IOException e) {
                        logger.warn("পুরানো ইমেজ ডিলিট করতে সমস্যা: {}", e.getMessage());
                    }
                }

                // নতুন ইমেজ সেভ
                String originalFilename = image.getOriginalFilename();
                String extension = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";

                String uniqueFilename = UUID.randomUUID() + extension;
                Path newFilePath = uploadPath.resolve(uniqueFilename);

                Files.copy(image.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("নতুন ইমেজ সেভ হয়েছে: {}", newFilePath);

                room.setImagePath("/" + uploadDir + uniqueFilename);

            } catch (IOException e) {
                logger.error("ইমেজ আপলোড ফেল হয়েছে", e);
                throw new IOException("ইমেজ আপলোড করতে সমস্যা: " + e.getMessage(), e);
            }
        }

        // ৫. সেভ করে রিটার্ন
        Room updatedRoom = roomRepository.save(room);
        logger.info("রুম আপডেট হয়েছে → ID: {}, Title: {}", updatedRoom.getId(), updatedRoom.getTitle());

        return updatedRoom;
    }

    /**
     * রুম ডিলিট করা
     */
    public void deleteRoom(Long roomId, User host) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("রুম ID " + roomId + " পাওয়া যায়নি"));

        if (!room.getHost().getId().equals(host.getId())) {
            throw new RuntimeException("এই রুম আপনার নয়");
        }

        // পুরানো ইমেজ ডিলিট
        if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
            try {
                String fileName = room.getImagePath().substring(room.getImagePath().lastIndexOf("/") + 1);
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warn("পুরানো ইমেজ ডিলিট করতে সমস্যা: {}", e.getMessage());
            }
        }

        roomRepository.delete(room);
    }

    @Transactional
    public Room getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Hibernate.initialize(room.getAdditionalImages());

        return room;
    }
}