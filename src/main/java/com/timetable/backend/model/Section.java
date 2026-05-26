package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EqualsAndHashCode(exclude = {"subjects", "electiveGroups"})
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

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(mappedBy = "sections")
    private Set<Subject> subjects = new HashSet<>();

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(mappedBy = "sections")
    private Set<ElectiveGroup> electiveGroups = new HashSet<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Booking> bookings;
}
