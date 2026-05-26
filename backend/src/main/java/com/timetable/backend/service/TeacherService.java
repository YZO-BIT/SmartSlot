package com.timetable.backend.service;

import com.timetable.backend.model.Teacher;
import com.timetable.backend.model.PredefinedSlot;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.Section;
import com.timetable.backend.repository.PredefinedSlotRepository;
import com.timetable.backend.repository.TeacherRepository;
import com.timetable.backend.repository.TeachingAssignmentRepository;
import com.timetable.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PredefinedSlotRepository predefinedSlotRepository;

    @Autowired
    private TeachingAssignmentRepository assignmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Optional<Teacher> getTeacherById(Long id) {
        return teacherRepository.findById(id);
    }

    public Optional<Teacher> findByUsername(String username) {
        return teacherRepository.findByUsername(username);
    }
    public Teacher saveTeacher(Teacher teacher) {
        boolean isNew = (teacher.getId() == null);
        Teacher saved = teacherRepository.save(teacher);
        if (isNew) {
            generatePredefinedSlotsForTeacher(saved);
        }
        return saved;
    }

    @Transactional
    public void deleteTeacher(Long id) {
        // Cleanup associated data
        assignmentRepository.deleteByTeacherId(id);
        bookingRepository.deleteByTeacherId(id);
        teacherRepository.deleteById(id);
    }

    public Optional<Teacher> findByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }

    private void generatePredefinedSlotsForTeacher(Teacher teacher) {
        // For each subject the teacher is expert in, create a predefined slot for each linked section.
        if (teacher.getExpertise() == null || teacher.getExpertise().isEmpty()) {
            return;
        }
        int slotCounter = 1;
        for (Subject subject : teacher.getExpertise()) {
            // If the subject is linked to sections, generate a slot per section.
            if (subject.getSections() != null && !subject.getSections().isEmpty()) {
                for (Section sec : subject.getSections()) {
                    PredefinedSlot slot = new PredefinedSlot();
                    slot.setTeacher(teacher);
                    slot.setSubject(subject);
                    slot.setSection(sec);
                    slot.setSlotId(slotCounter++);

                    predefinedSlotRepository.save(slot);
                }
            } else {
                // No specific sections, create a generic slot.
                PredefinedSlot slot = new PredefinedSlot();
                slot.setTeacher(teacher);
                slot.setSubject(subject);
                slot.setSlotId(slotCounter++);

                predefinedSlotRepository.save(slot);
            }
        }
    }
}

