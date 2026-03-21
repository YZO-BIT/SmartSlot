package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = true)
    private Section section;

    @ManyToOne
    @JoinColumn(name = "elective_group_id", nullable = true)
    private ElectiveGroup electiveGroup;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private int slotId;
    private LocalDate bookingDate;

    /**
     * Determines if this booking is for an elective group or a standard section
     * @return true if booking is for an elective group, false if for a single section
     */
    public boolean isElectiveBooking() {
        return this.electiveGroup != null && this.section == null;
    }

    /**
     * Determines if this booking is for a standard section
     * @return true if booking is for a single section, false if for an elective group
     */
    public boolean isSectionBooking() {
        return this.section != null && this.electiveGroup == null;
    }
}
