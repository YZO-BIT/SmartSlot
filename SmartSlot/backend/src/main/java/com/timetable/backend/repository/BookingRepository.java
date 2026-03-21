package com.timetable.backend.repository;

import com.timetable.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByRoomIdAndSlotIdAndBookingDate(Long roomId, int slotId, LocalDate date);
    
    boolean existsByTeacherIdAndSlotIdAndBookingDate(Long teacherId, int slotId, LocalDate date);

    List<Booking> findByTeacherIdAndBookingDateOrderBySlotIdAsc(Long teacherId, LocalDate date);
}