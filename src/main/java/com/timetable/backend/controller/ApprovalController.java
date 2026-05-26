package com.timetable.backend.controller;

import com.timetable.backend.model.Teacher;
import com.timetable.backend.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/approvals")
@CrossOrigin("*")
public class ApprovalController {

    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * Get pending users.
     * Admin sees everyone.
     * HOD sees only Teachers.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Teacher>> getPendingUsers(@RequestParam(required = false) String requesterRole) {
        List<Teacher> pending = teacherRepository.findAll().stream()
                .filter(t -> !t.isApproved())
                .collect(Collectors.toList());

        if ("HOD".equalsIgnoreCase(requesterRole)) {
            // HODs can only approve Teachers
            pending = pending.stream()
                    .filter(t -> "TEACHER".equalsIgnoreCase(t.getRole()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(pending);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveUser(@PathVariable Long id) {
        return teacherRepository.findById(id)
                .map(teacher -> {
                    teacher.setApproved(true);
                    teacherRepository.save(teacher);
                    return ResponseEntity.ok("User approved successfully!");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reject/{id}")
    public ResponseEntity<String> rejectUser(@PathVariable Long id) {
        if (teacherRepository.existsById(id)) {
            teacherRepository.deleteById(id);
            return ResponseEntity.ok("User rejected and removed.");
        }
        return ResponseEntity.notFound().build();
    }
}
