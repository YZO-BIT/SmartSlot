package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"sections"})
@Entity
@Table(name = "teaching_assignments")
public class TeachingAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Grouped sections for this assignment slot.
     * A single slot can cover multiple sections (e.g. A1 + A2 studying together).
     * Each TeachingAssignment row represents ONE independent lecture slot group.
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"electiveGroups", "subjects", "bookings"})
    @ManyToMany
    @JoinTable(
            name = "teaching_assignment_sections",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    private Set<Section> sections = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Returns a display label for all sections in this grouped slot.
     */
    public String getSectionGroupLabel() {
        if (sections == null || sections.isEmpty()) return "N/A";
        return sections.stream()
                .map(Section::getName)
                .sorted()
                .reduce((a, b) -> a + " + " + b)
                .orElse("N/A");
    }
}
