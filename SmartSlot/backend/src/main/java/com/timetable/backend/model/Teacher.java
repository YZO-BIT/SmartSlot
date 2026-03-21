package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String department;
    private boolean isPriorityUser;
    private int maxCombinedSections;

    @Column(columnDefinition = "boolean default false")
    private boolean hasAudiAccess = false;

    @ManyToMany
    @JoinTable(
            name = "teacher_expertise",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> expertise = new HashSet<>();
}
