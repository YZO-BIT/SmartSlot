package com.timetable.backend.repository;

import com.timetable.backend.model.ElectiveGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ElectiveGroupRepository extends JpaRepository<ElectiveGroup, Long> {

    /**
     * Find elective group by name
     */
    Optional<ElectiveGroup> findByGroupName(String groupName);

    /**
     * Optimized query: Find all elective groups that contain a specific section
     * Uses JOIN to avoid N+1 queries
     */
    @Query("SELECT DISTINCT eg FROM ElectiveGroup eg " +
           "JOIN eg.sections s WHERE s.id = :sectionId")
    List<ElectiveGroup> findGroupsBySection(@Param("sectionId") Long sectionId);

    /**
     * Optimized query: Check if section is already booked during a slot while attending an elective
     * Returns elective groups that have bookings in the specified slot
     */
    @Query("SELECT DISTINCT eg FROM ElectiveGroup eg " +
           "JOIN eg.sections s " +
           "JOIN Booking b ON b.electiveGroup = eg " +
           "WHERE s.id = :sectionId AND b.slotId = :slotId AND b.bookingDate = :bookingDate")
    List<ElectiveGroup> findConflictingElectiveGroups(@Param("sectionId") Long sectionId, 
                                                      @Param("slotId") int slotId, 
                                                      @Param("bookingDate") LocalDate bookingDate);

    /**
     * Check if any section in an elective group has a standard (non-elective) booking during a slot
     * Returns sections that already have standard class bookings
     */
    @Query("SELECT DISTINCT s FROM ElectiveGroup eg " +
           "JOIN eg.sections s " +
           "JOIN Booking b ON b.section = s " +
           "WHERE eg.id = :electiveGroupId AND b.slotId = :slotId AND b.bookingDate = :bookingDate " +
           "AND b.electiveGroup IS NULL")
    List<Object> findConflictingSectionsInGroup(@Param("electiveGroupId") Long electiveGroupId, 
                                                @Param("slotId") int slotId, 
                                                @Param("bookingDate") LocalDate bookingDate);
}
