package com.timetable.backend.repository;

import com.timetable.backend.model.Room;
import com.timetable.backend.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByRoomTypeIn(Set<RoomType> roomTypes);
}