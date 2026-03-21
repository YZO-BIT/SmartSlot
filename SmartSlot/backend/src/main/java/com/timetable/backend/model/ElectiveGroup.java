package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "elective_groups")
public class ElectiveGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupName;

    private String description;

    @Column(columnDefinition = "boolean default true")
    private boolean isActive = true;

    @ManyToMany
    @JoinTable(
            name = "elective_group_sections",
            joinColumns = @JoinColumn(name = "elective_group_id"),
            inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    private Set<Section> sections = new HashSet<>();

    /**
     * Calculates total student count across all sections in this elective group
     * @return sum of student counts from all mapped sections
     */
    public int getTotalStudentCount() {
        return sections.stream()
                .mapToInt(Section::getStudentCount)
                .sum();
    }
}
