package com.timetable.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherDTO {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String department;
    private Boolean priorityUser;
    private Integer maxCombinedSections;
    private Set<String> eligibleRoomTypes; // RoomType enum strings
    private java.util.Set<TeachingAssignmentDTO> assignments; 
    private Set<String> expertise; // Subject names from frontend
}
