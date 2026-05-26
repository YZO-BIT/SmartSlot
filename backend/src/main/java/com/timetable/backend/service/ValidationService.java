package com.timetable.backend.service;

import com.timetable.backend.dto.ValidationReport;
import com.timetable.backend.model.*;
import com.timetable.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationService {

    // Constants
    private static final int MAX_WEEKLY_SLOTS = 40; // 5 days * 8 slots/day

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SectionRepository sectionRepository;

    /**
     * Validates room availability for all subjects before genetic algorithm execution.
     * Checks if database contains at least one Room for each Subject's required room type.
     *
     * @return ValidationReport containing feasibility status and error messages
     */
    public ValidationReport validateRoomAvailability() {
        ValidationReport report = new ValidationReport();

        // Fetch all subjects from database
        List<Subject> subjects = subjectRepository.findAll();

        if (subjects.isEmpty()) {
            report.addError("No subjects found in the database. Please add at least one subject.");
            return report;
        }

        // Iterate through each subject
        for (Subject subject : subjects) {
            RoomType requiredRoomType = subject.getRoomTypeRequirement();

            // Check if at least one room with matching type exists
            boolean roomExists = roomRepository.findAll().stream()
                    .anyMatch(room -> room.getRoomType() == requiredRoomType);

            if (!roomExists) {
                String errorMessage = String.format(
                        "Subject '%s' requires room type '%s (%s)', but no rooms of this type are available in the database.",
                        subject.getName(),
                        requiredRoomType.name(),
                        requiredRoomType.getDescription()
                );
                report.addError(errorMessage);
            }
        }

        return report;
    }

    /**
     * Validates if a specific room type has at least one room available.
     *
     * @param roomType the room type to validate
     * @return true if rooms of this type exist, false otherwise
     */
    public boolean isRoomTypeAvailable(RoomType roomType) {
        return roomRepository.findAll().stream()
                .anyMatch(room -> room.getRoomType() == roomType);
    }

    /**
     * Gets the count of available rooms for a specific room type.
     *
     * @param roomType the room type to check
     * @return count of rooms matching the room type
     */
    public long countRoomsByType(RoomType roomType) {
        return roomRepository.findAll().stream()
                .filter(room -> room.getRoomType() == roomType)
                .count();
    }

    /**
     * Task 1: Validates teacher expertise for subject assignments.
     * Checks if every teacher assigned to a subject has that subject in their expertise.
     *
     * If a mismatch is found (e.g., a Physics teacher assigned to History),
     * reports: "Expertise Mismatch: Teacher [Name] is not qualified for [Subject]."
     *
     * @return ValidationReport containing feasibility status and error messages
     */
    public ValidationReport validateTeacherExpertise() {
        ValidationReport report = new ValidationReport();

        // Fetch all bookings from database
        List<Booking> bookings = bookingRepository.findAll();

        if (bookings.isEmpty()) {
            report.getErrorMessages().add("No bookings found in the database.");
            return report;
        }

        // Use Java 8 Streams to validate teacher expertise
        bookings.stream()
                .filter(booking -> booking.getTeacher() != null && booking.getSubject() != null)
                .forEach(booking -> {
                    Teacher teacher = booking.getTeacher();
                    Subject subject = booking.getSubject();

                    // Check if teacher has this subject in their expertise
                    boolean isQualified = teacher.getExpertise().stream()
                            .anyMatch(expertiseSubject -> expertiseSubject.getId().equals(subject.getId()));

                    if (!isQualified) {
                        String errorMessage = String.format(
                                "Expertise Mismatch: Teacher '%s' is not qualified for subject '%s'.",
                                teacher.getName(),
                                subject.getName()
                        );
                        report.addError(errorMessage);
                    }
                });

        return report;
    }

    /**
     * Task 2: Validates section capacity for weekly slots.
     * For each Section, sums up the total required lectures per week across all subjects.
     *
     * MAX_WEEKLY_SLOTS = 40 (5 days * 8 slots per day)
     *
     * If Total_Required_Lectures > MAX_WEEKLY_SLOTS,
     * returns a Hard Error: "Schedule Impossible: Section [Name] requires [X] slots but only [Y] are available."
     *
     * @return ValidationReport containing feasibility status and error messages
     */
    public ValidationReport validateSectionCapacity() {
        ValidationReport report = new ValidationReport();

        // Fetch all sections from database
        List<Section> sections = sectionRepository.findAll();

        if (sections.isEmpty()) {
            report.getErrorMessages().add("No sections found in the database.");
            return report;
        }

        // Use Java 8 Streams for summation logic
        sections.stream()
                .filter(section -> section.getSubjects() != null && !section.getSubjects().isEmpty())
                .forEach(section -> {
                    // Sum up total required lectures per week
                    int totalRequiredSlots = section.getSubjects().stream()
                            .mapToInt(Subject::getLecturesPerWeek)
                            .sum();

                    // Check if total exceeds maximum available slots
                    if (totalRequiredSlots > MAX_WEEKLY_SLOTS) {
                        String errorMessage = String.format(
                                "Schedule Impossible: Section '%s' requires %d slots per week but only %d are available.",
                                section.getName(),
                                totalRequiredSlots,
                                MAX_WEEKLY_SLOTS
                        );
                        report.addError(errorMessage);
                    }
                });

        return report;
    }

    /**
     * Comprehensive validation combining all consistency checks.
     * Prevents Ghost Bookings by validating all data dependencies.
     *
     * @return ValidationReport with all validation results
     */
    public ValidationReport validateAllDataConsistency() {
        ValidationReport report = new ValidationReport();

        // Run all validation methods
        ValidationReport roomReport = validateRoomAvailability();
        ValidationReport expertiseReport = validateTeacherExpertise();
        ValidationReport capacityReport = validateSectionCapacity();

        // Aggregate all errors
        report.getErrorMessages().addAll(roomReport.getErrorMessages());
        report.getErrorMessages().addAll(expertiseReport.getErrorMessages());
        report.getErrorMessages().addAll(capacityReport.getErrorMessages());

        // Set feasibility based on presence of errors
        if (!report.getErrorMessages().isEmpty()) {
            report.setFeasible(false);
        }

        return report;
    }
}
