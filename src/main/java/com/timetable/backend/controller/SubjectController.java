package com.timetable.backend.controller;

import com.timetable.backend.model.Subject;
import com.timetable.backend.repository.SubjectRepository;
import com.timetable.backend.repository.BookingRepository;
import com.timetable.backend.repository.TeachingAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin("*")
public class SubjectController {
    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TeachingAssignmentRepository assignmentRepository;

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    @PostMapping
    public Subject createSubject(@RequestBody Subject subject) {
        return subjectRepository.save(subject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @RequestBody Subject details) {
        return subjectRepository.findById(id).map(s -> {
            s.setName(details.getName());
            s.setRoomTypeRequirement(details.getRoomTypeRequirement());
            s.setLecturesPerWeek(details.getLecturesPerWeek());
            s.setElective(details.isElective());
            return ResponseEntity.ok(subjectRepository.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        // Clear related data to avoid foreign key constraints
        bookingRepository.deleteBySubjectId(id);
        
        List<com.timetable.backend.model.TeachingAssignment> assignments = assignmentRepository.findBySubjectId(id);
        for (com.timetable.backend.model.TeachingAssignment asgn : assignments) {
            assignmentRepository.delete(asgn);
        }
        
        subjectRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
