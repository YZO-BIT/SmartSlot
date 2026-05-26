package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a slot that the admin predefines for a teacher.
 * Teachers can request to book these slots; the request flow is handled elsewhere.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "predefined_slots")
public class PredefinedSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Identifier of the slot within the day (e.g., 1‑10).
     */
    private int slotId;

    /**
     * Indicates whether the slot is still available for request.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = true)
    private Section section;
}
