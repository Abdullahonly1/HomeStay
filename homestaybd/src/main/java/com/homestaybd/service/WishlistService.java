package com.homestaybd.service;

import com.homestaybd.model.Room;
import com.homestaybd.model.Wishlist;
import com.homestaybd.repository.RoomRepository;
import com.homestaybd.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private RoomRepository roomRepository;

    public void addToWishlist(Long userId, Long roomId) {
        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setRoomId(roomId);
        wishlistRepository.save(wishlist);
    }

    public void removeFromWishlist(Long userId, Long roomId) {
        wishlistRepository.deleteByUserIdAndRoomId(userId, roomId);
    }

    public List<Room> getWishlistRooms(Long userId) {
        List<Long> roomIds = wishlistRepository.findRoomIdsByUserId(userId);
        return roomRepository.findAllById(roomIds);
    }
}
