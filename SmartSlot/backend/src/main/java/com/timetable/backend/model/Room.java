package com.timetable.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomNumber;

    private int capacity;

    private String roomCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;
}
