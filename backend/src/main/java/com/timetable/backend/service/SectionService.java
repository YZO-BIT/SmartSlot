package com.timetable.backend.service;

import com.timetable.backend.model.Section;
import com.timetable.backend.repository.SectionRepository;
import com.timetable.backend.repository.TeachingAssignmentRepository;
import com.timetable.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SectionService {
    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private TeachingAssignmentRepository assignmentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    public Section saveSection(Section section) {
        return sectionRepository.save(section);
    }

    public Optional<Section> getSectionById(Long id) {
        return sectionRepository.findById(id);
    }

    @Transactional
    public void deleteSection(Long id) {
        // For ManyToMany: find all assignments that include this section
        // Remove section from their set; delete assignment entirely if it had only this one section
        java.util.List<com.timetable.backend.model.TeachingAssignment> affected =
                assignmentRepository.findAssignmentsContainingSection(id);

        for (com.timetable.backend.model.TeachingAssignment asgn : affected) {
            asgn.getSections().removeIf(s -> s.getId().equals(id));
            if (asgn.getSections().isEmpty()) {
                assignmentRepository.delete(asgn);
            } else {
                assignmentRepository.save(asgn);
            }
        }

        // Delete bookings referencing this section
        bookingRepository.deleteBySectionId(id);

        // Now safe to delete the section itself
        sectionRepository.deleteById(id);
    }
}
