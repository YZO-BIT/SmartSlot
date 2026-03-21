package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomTypeRequirement;

    private String description;

    @Column(nullable = false)
    private int lecturesPerWeek = 3; // Default: 3 lectures per week

    @Column(columnDefinition = "boolean default false")
    private boolean isElective = false; // True if this is an elective subject

    @ManyToMany(mappedBy = "expertise")
    private Set<Teacher> teachers = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "section_subjects",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "section_id")
    )
    private Set<Section> sections = new HashSet<>();
}
