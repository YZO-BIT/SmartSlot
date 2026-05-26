package com.timetable.backend.repository;

import com.timetable.backend.model.PredefinedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredefinedSlotRepository extends JpaRepository<PredefinedSlot, Long> {
    // Additional query methods can be added as needed
}
