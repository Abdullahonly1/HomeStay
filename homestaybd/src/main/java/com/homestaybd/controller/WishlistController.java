package com.homestaybd.controller;

import com.homestaybd.dto.RoomDTO;
import com.homestaybd.model.Room;
import com.homestaybd.model.User;
import com.homestaybd.service.UserService;
import com.homestaybd.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    // Add to wishlist
    @PostMapping("/add/{roomId}")
    public ResponseEntity<String> addToWishlist(@PathVariable Long roomId, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        wishlistService.addToWishlist(user.getId(), roomId);
        return ResponseEntity.ok("Added to wishlist");
    }

    // Remove from wishlist
    @PostMapping("/remove/{roomId}")
    public ResponseEntity<String> removeFromWishlist(@PathVariable Long roomId, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        wishlistService.removeFromWishlist(user.getId(), roomId);
        return ResponseEntity.ok("Removed from wishlist");
    }

    // Get my wishlist
    @GetMapping("/my")
    public ResponseEntity<List<RoomDTO>> getMyWishlist(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        List<Room> wishlistRooms = wishlistService.getWishlistRooms(user.getId());
        List<RoomDTO> dtos = wishlistRooms.stream().map(RoomDTO::fromEntity).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}