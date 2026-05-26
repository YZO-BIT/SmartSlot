package com.timetable.backend.controller;
import org.springframework.http.ResponseEntity;

import com.timetable.backend.model.Room;
import com.timetable.backend.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/all")
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomService.saveRoom(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails) {
        return roomService.getRoomById(id)
                .map(room -> {
                    room.setRoomNumber(roomDetails.getRoomNumber());
                    room.setCapacity(roomDetails.getCapacity());
                    room.setRoomCategory(roomDetails.getRoomCategory());
                    room.setRoomType(roomDetails.getRoomType());
                    return ResponseEntity.ok(roomService.saveRoom(room));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok().build();
    }
}
