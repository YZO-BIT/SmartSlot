package com.timetable.backend.service;

import com.timetable.backend.model.Room;
import com.timetable.backend.repository.RoomRepository;
import com.timetable.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public void deleteRoom(Long id) {
        try {
            bookingRepository.deleteByRoomId(id);
        } catch (Exception e) {
            // Log as needed
        }
        roomRepository.deleteById(id);
    }

    public List<Room> getEligibleRooms(java.util.Set<com.timetable.backend.model.RoomType> types) {
        if (types == null || types.isEmpty()) return java.util.Collections.emptyList();
        return roomRepository.findByRoomTypeIn(types);
    }
}
