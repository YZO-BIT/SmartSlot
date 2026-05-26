package com.timetable.backend.controller;

import com.timetable.backend.dto.TeacherDTO;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.Teacher;
import com.timetable.backend.repository.SectionRepository;
import com.timetable.backend.repository.SubjectRepository;
import com.timetable.backend.repository.TeachingAssignmentRepository;
import com.timetable.backend.service.RoomService;
import com.timetable.backend.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*") // For development convenience
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TeachingAssignmentRepository assignmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        return teacherService.getTeacherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> createTeacher(@RequestBody TeacherDTO teacherDto) {
        if (teacherService.findByUsername(teacherDto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (teacherService.findByEmail(teacherDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        Teacher teacher = new Teacher();
        mapDtoToEntity(teacherDto, teacher);
        Teacher saved = teacherService.saveTeacher(teacher);
        saveAssignments(teacherDto, saved);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody TeacherDTO teacherDto) {
        java.util.Optional<Teacher> teacherOpt = teacherService.getTeacherById(id);
        if (teacherOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if username/email is taken by another teacher
        java.util.Optional<Teacher> byUsername = teacherService.findByUsername(teacherDto.getUsername());
        if (byUsername.isPresent() && !byUsername.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        java.util.Optional<Teacher> byEmail = teacherService.findByEmail(teacherDto.getEmail());
        if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        Teacher teacher = teacherOpt.get();
        mapDtoToEntity(teacherDto, teacher);
        Teacher saved = teacherService.saveTeacher(teacher);
        saveAssignments(teacherDto, saved);
        return ResponseEntity.ok(saved);
    }

    private void saveAssignments(TeacherDTO dto, Teacher teacher) {
        // Clear existing assignments for this teacher
        assignmentRepository.deleteByTeacherId(teacher.getId());

        if (dto.getAssignments() != null) {
            for (com.timetable.backend.dto.TeachingAssignmentDTO asgnDto : dto.getAssignments()) {
                java.util.List<Long> sectionIds = asgnDto.getSectionIds();
                Long subjectId = asgnDto.getSubjectId();

                if (sectionIds == null || sectionIds.isEmpty() || subjectId == null) continue;

                com.timetable.backend.model.Subject subject = subjectRepository.findById(subjectId).orElse(null);
                if (subject == null) continue;

                // Resolve all sections for this grouped slot
                java.util.Set<com.timetable.backend.model.Section> sections = new java.util.HashSet<>();
                for (Long secId : sectionIds) {
                    sectionRepository.findById(secId).ifPresent(sections::add);
                }

                if (sections.isEmpty()) continue;

                // One TeachingAssignment = one independent grouped slot
                com.timetable.backend.model.TeachingAssignment asgn = new com.timetable.backend.model.TeachingAssignment();
                asgn.setTeacher(teacher);
                asgn.setSections(sections);
                asgn.setSubject(subject);
                assignmentRepository.save(asgn);
            }
        }
    }

    private void mapDtoToEntity(TeacherDTO dto, Teacher entity) {
        entity.setName(dto.getName());
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        
        // 🔒 Only update password if a non-empty one is provided
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            entity.setPassword(dto.getPassword());
        } else if (entity.getPassword() == null) {
            entity.setPassword("password123"); // Default for new teachers if blank
        }
        
        entity.setDepartment(dto.getDepartment());
        
        // 🔒 Preserve existing role if present (e.g., ADMIN or HOD)
        if (entity.getRole() == null) {
            entity.setRole("TEACHER");
        }
        
        entity.setPriorityUser(Boolean.TRUE.equals(dto.getPriorityUser()));
        entity.setMaxCombinedSections(dto.getMaxCombinedSections() != null ? dto.getMaxCombinedSections() : 0);
        
        // Map Room Types
        if (dto.getEligibleRoomTypes() != null) {
            entity.getEligibleRoomTypes().clear();
            for (String rtStr : dto.getEligibleRoomTypes()) {
                try {
                    entity.getEligibleRoomTypes().add(com.timetable.backend.model.RoomType.valueOf(rtStr));
                } catch (Exception e) {
                    System.err.println("Invalid room type skipped: " + rtStr);
                }
            }
        }
        
        if (dto.getExpertise() != null) {
            Set<Subject> subjects = dto.getExpertise().stream()
                    .map(name -> subjectRepository.findByName(name)
                            .orElseGet(() -> {
                                Subject newSub = new Subject();
                                newSub.setName(name);
                                return subjectRepository.save(newSub);
                            }))
                    .collect(Collectors.toSet());
            entity.setExpertise(subjects);
        }
    }

    @GetMapping("/{id}/workload")
    public ResponseEntity<List<com.timetable.backend.model.TeachingAssignment>> getTeacherWorkload(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentRepository.findByTeacherId(id));
    }

    @GetMapping("/{id}/eligible-rooms")
    public ResponseEntity<List<com.timetable.backend.model.Room>> getEligibleRooms(@PathVariable Long id) {
        return teacherService.getTeacherById(id)
                .map(teacher -> ResponseEntity.ok(roomService.getEligibleRooms(teacher.getEligibleRoomTypes())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/workloads/all")
    public List<com.timetable.backend.model.TeachingAssignment> getAllWorkloads() {
        return assignmentRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok().build();
    }
}
