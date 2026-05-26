package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@EqualsAndHashCode(exclude = {"expertise"})
@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique = true)
    private String username;
    private String email;
    private String phone;
    private String password;
    private String department;
    private String role; // "ADMIN", "HOD", "TEACHER"
    
    @Column(columnDefinition = "boolean default true")
    private boolean isApproved = true;
    
    private boolean isPriorityUser;
    private int maxCombinedSections;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "teacher_room_permissions", joinColumns = @JoinColumn(name = "teacher_id"))
    private Set<RoomType> eligibleRoomTypes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "teacher_expertise",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> expertise = new HashSet<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Booking> bookings;
}
