package com.homestaybd.controller;

import com.homestaybd.dto.RoomDTO;
import com.homestaybd.dto.RoomUpdateRequest;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import com.homestaybd.service.RoomService;
import com.homestaybd.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);

    public RoomController(RoomService roomService, UserService userService) {
        this.roomService = roomService;
        this.userService = userService;
    }

    // হোস্টের নিজের রুম লিস্ট
    @GetMapping("/my")
    public ResponseEntity<List<RoomDTO>> getMyRooms(Principal principal) {
        if (principal == null) {
            logger.warn("No principal found - unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = principal.getName();
        User host = userService.findByUsername(username);

        if (host == null) {
            logger.error("User not found: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Room> rooms = roomService.getRoomsByHost(host.getId());

        List<RoomDTO> roomDTOs = rooms.stream()
                .map(room -> {
                    RoomDTO dto = new RoomDTO(room);
                    dto.setAdditionalImages(room.getAdditionalImages());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(roomDTOs);
    }

    // নতুন রুম যোগ (একাধিক ছবি সাপোর্ট)
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<String> addRoom(
            @RequestPart("title") String title,
            @RequestPart("description") String description,
            @RequestPart(value = "facilities", required = false) String facilities,
            @RequestPart("location") String location,
            @RequestPart("price") String priceStr,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            Authentication authentication) {

        logger.info("===== /api/rooms/add POST API কল হয়েছে =====");
        logger.info("Params → title='{}', location='{}', priceStr='{}', facilities='{}', description='{}'",
                title, location, priceStr, facilities, description);

        try {
            // Authentication চেক
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal().toString())) {
                logger.warn("Unauthorized access attempt");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("দয়া করে লগইন করুন");
            }

            String username = authentication.getName();
            logger.info("Authenticated user: {}", username);

            User currentHost = userService.findByUsername(username);

            // Price convert
            double price;
            try {
                price = Double.parseDouble(priceStr.trim());
                if (price <= 0) throw new NumberFormatException("Price must be positive");
            } catch (NumberFormatException e) {
                logger.error("Invalid price format: {}", priceStr);
                return ResponseEntity.badRequest().body("দাম সঠিকভাবে দিন (শুধু সংখ্যা)");
            }

            // Room entity
            Room room = new Room();
            room.setTitle(title.trim());
            room.setDescription(description.trim());
            room.setFacilities(facilities != null ? facilities.trim() : "");
            room.setLocation(location.trim());
            room.setPrice(price);
            room.setHost(currentHost);
            room.setAvailable(true);

            // Image upload
            String uploadDir = "uploads/rooms/";
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath);
            }

            String mainImagePath = null;
            List<String> additionalImagePaths = new ArrayList<>();

            if (images != null && images.length > 0) {
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (image != null && !image.isEmpty()) {
                        try {
                            String originalName = image.getOriginalFilename();
                            String extension = originalName != null ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
                            String uniqueName = UUID.randomUUID() + extension;
                            Path filePath = uploadPath.resolve(uniqueName);

                            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                            logger.info("Image saved: {}", filePath);

                            String dbPath = "/" + uploadDir + uniqueName;

                            if (i == 0) {
                                mainImagePath = dbPath;
                                room.setImagePath(mainImagePath);
                            } else {
                                additionalImagePaths.add(dbPath);
                            }
                        } catch (IOException e) {
                            logger.error("Failed to save image {}: {}", i + 1, e.getMessage(), e);
                        }
                    }
                }
            }

            if (mainImagePath == null) {
                room.setImagePath("/images/placeholder-room.jpg");
                logger.warn("No valid image → placeholder used");
            }

            if (!additionalImagePaths.isEmpty()) {
                room.setAdditionalImages(additionalImagePaths);
                logger.info("Additional images: {} paths", additionalImagePaths.size());
            }

            // Save room
            Room savedRoom = roomService.saveRoom(room);
            logger.info("Room added → ID={}, MainImage={}, Additional={}",
                    savedRoom.getId(), savedRoom.getImagePath(),
                    savedRoom.getAdditionalImages() != null ? savedRoom.getAdditionalImages().size() : 0);

            return ResponseEntity.ok("রুম সফলভাবে যোগ হয়েছে! ID: " + savedRoom.getId());

        } catch (Exception e) {
            logger.error("Room add failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("রুম যোগ করতে সমস্যা: " + e.getMessage());
        }
    }

    // সব রুম (লোকেশন ফিল্টার সহ)
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms(
            @RequestParam(required = false) String location,
            Principal principal) {

        List<Room> rooms;

        if (location != null && !location.isEmpty()) {
            rooms = roomService.findByLocationContainingIgnoreCase(location);
        } else {
            rooms = roomService.findAllAvailable();
        }

        List<RoomDTO> roomDTOs = rooms.stream().map(room -> {
            RoomDTO dto = new RoomDTO(room);

            // isWishlisted চেক (যদি লগইন থাকে)
            if (principal != null) {
                String username = principal.getName();
                User user = userService.findByUsername(username);
                if (user != null) {
                    // boolean isWishlisted = wishlistRepository.existsByUserIdAndRoomId(user.getId(), room.getId());
                    // dto.setIsWishlisted(isWishlisted);
                    // ↑ যদি wishlistRepository থাকে তাহলে খুলে দাও
                }
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(roomDTOs);
    }

    // একটা রুমের ডিটেইল
    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);

        logger.info("Room loaded → ID: {}, Main image: {}, Additional images: {}",
                room.getId(), room.getImagePath(), room.getAdditionalImages().size());

        RoomDTO dto = new RoomDTO(room);
        return ResponseEntity.ok(dto);
    }

    // রুম আপডেট
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long id,
            @ModelAttribute RoomUpdateRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) throws IOException {

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthorized update attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User host = userService.findByUsername(username);

        if (host == null) {
            logger.error("User not found: {}", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Room updatedRoom = roomService.updateRoom(id, request, image, host);
        logger.info("Room updated successfully → ID: {}", updatedRoom.getId());

        return ResponseEntity.ok(updatedRoom);
    }

    // রুম ডিলিট
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthorized delete attempt");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User host = userService.findByUsername(username);

        if (host == null) {
            logger.error("User not found: {}", username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        roomService.deleteRoom(id, host);
        logger.info("Room deleted → ID: {}", id);

        return ResponseEntity.noContent().build();
    }
}