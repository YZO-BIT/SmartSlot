package com.timetable.backend.repository;

import com.timetable.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByRoomIdAndSlotIdAndBookingDateAndStatusNot(Long roomId, int slotId, LocalDate date, String status);

    boolean existsByTeacherIdAndSlotIdAndBookingDateAndStatusNot(Long teacherId, int slotId, LocalDate date,
            String status);

    boolean existsBySectionIdAndSlotIdAndBookingDateAndStatusNot(Long sectionId, int slotId, LocalDate date,
            String status);

    boolean existsBySectionIdAndSubjectIdAndBookingDateAndStatusNot(Long sectionId, Long subjectId, LocalDate date, String status);

    List<Booking> findByTeacherIdAndBookingDateOrderBySlotIdAsc(Long teacherId, LocalDate date);

    List<Booking> findByRoomIdAndBookingDateOrderBySlotIdAsc(Long roomId, LocalDate date);

    List<Booking> findBySectionIdAndBookingDateOrderBySlotIdAsc(Long sectionId, LocalDate date);

    List<Booking> findByTeacherIdAndBookingDateBetweenOrderByBookingDateAscSlotIdAsc(Long teacherId, LocalDate start,
            LocalDate end);

    List<Booking> findByRoomIdAndBookingDateBetweenOrderByBookingDateAscSlotIdAsc(Long roomId, LocalDate start,
            LocalDate end);

    List<Booking> findBySectionIdAndBookingDateBetweenOrderByBookingDateAscSlotIdAsc(Long sectionId, LocalDate start,
            LocalDate end);

    long countBySectionIdAndSubjectIdAndBookingDateBetween(Long sectionId, Long subjectId, LocalDate start,
            LocalDate end);

    long countBySectionIdAndSubjectIdAndBookingDateBetweenAndStatusNot(Long sectionId, Long subjectId, LocalDate start,
            LocalDate end, String status);

    @Transactional
    void deleteByRoomId(Long roomId);

    @Transactional
    void deleteByTeacherId(Long teacherId);

    @Transactional
    void deleteBySectionId(Long sectionId);

    @Transactional
    void deleteBySubjectId(Long subjectId);
}