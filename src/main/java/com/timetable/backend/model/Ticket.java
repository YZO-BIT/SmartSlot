package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private String reason;

    @Column(length = 1000)
    private String conflictDetails;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.PENDING;

    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime approvedDate;

    // Specific request targets
    private java.time.LocalDate requestedDate;
    private Integer requestedSlotId;
    
    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section requestedSection;

    public enum TicketStatus {
        PENDING, APPROVED, REJECTED
    }
}
