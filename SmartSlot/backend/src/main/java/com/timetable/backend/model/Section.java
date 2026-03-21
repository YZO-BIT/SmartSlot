package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "sections")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int batchYear;

    @Column(nullable = false)
    private int studentCount = 50; // Default: 50 students per section

    @ManyToMany(mappedBy = "sections")
    private Set<Subject> subjects = new HashSet<>();

    @ManyToMany(mappedBy = "sections")
    private Set<ElectiveGroup> electiveGroups = new HashSet<>();
}
