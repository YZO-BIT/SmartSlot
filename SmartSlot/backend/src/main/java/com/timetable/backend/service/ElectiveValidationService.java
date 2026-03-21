package com.timetable.backend.service;

import com.timetable.backend.dto.ValidationReport;
import com.timetable.backend.model.*;
import com.timetable.backend.repository.ElectiveGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElectiveValidationService {

    @Autowired
    private ElectiveGroupRepository electiveGroupRepository;

    /**
     * Task 2 - Condition A: Check if booking an elective group conflicts with existing section bookings
     * 
     * If booking a slot for ElectiveGroup G1, check if any of its mapped Sections 
     * already have a standard class booked in that exact slot.
     *
     * @param booking the elective group booking request
     * @return ValidationReport with conflict details if any
     */
    public ValidationReport validateElectiveGroupConflict(Booking booking) {
        ValidationReport report = new ValidationReport();

        if (booking.getElectiveGroup() == null) {
            report.getErrorMessages().add("Elective group is null");
            return report;
        }

        // Use optimized JPA query to find conflicting sections
        List<Object> conflictingSections = electiveGroupRepository.findConflictingSectionsInGroup(
                booking.getElectiveGroup().getId(),
                booking.getSlotId(),
                booking.getBookingDate()
        );

        if (!conflictingSections.isEmpty()) {
            for (Object sectionObj : conflictingSections) {
                if (sectionObj instanceof Section) {
                    Section section = (Section) sectionObj;
                    String errorMessage = String.format(
                            "Conflict: Section '%s' inside this group is already busy with a standard class during this slot.",
                            section.getName()
                    );
                    report.addError(errorMessage);
                }
            }
        }

        return report;
    }

    /**
     * Task 2 - Condition B: Check if booking a standard section conflicts with existing elective bookings
     *
     * If booking a standard class for Section A, query the database to see if Section A 
     * belongs to any ElectiveGroup that is already booked for that slot.
     *
     * @param booking the standard section booking request
     * @return ValidationReport with conflict details if any
     */
    public ValidationReport validateSectionElectiveConflict(Booking booking) {
        ValidationReport report = new ValidationReport();

        if (booking.getSection() == null) {
            report.getErrorMessages().add("Section is null");
            return report;
        }

        // Use optimized JPA query to find conflicting elective groups
        List<ElectiveGroup> conflictingGroups = electiveGroupRepository.findConflictingElectiveGroups(
                booking.getSection().getId(),
                booking.getSlotId(),
                booking.getBookingDate()
        );

        if (!conflictingGroups.isEmpty()) {
            for (ElectiveGroup group : conflictingGroups) {
                String errorMessage = String.format(
                        "Conflict: This section is already attending Elective Group '%s' during this slot.",
                        group.getGroupName()
                );
                report.addError(errorMessage);
            }
        }

        return report;
    }

    /**
     * Task 3: Dynamic Room Capacity Check
     *
     * When an ElectiveGroup is booked, calculate the total student count by summing 
     * the sizes of all mapped Sections.
     *
     * Ensure requestedRoom.capacity >= combined total.
     *
     * @param booking the elective group booking request
     * @return ValidationReport with capacity errors if room is too small
     */
    public ValidationReport validateElectiveGroupCapacity(Booking booking) {
        ValidationReport report = new ValidationReport();

        if (booking.getElectiveGroup() == null || booking.getRoom() == null) {
            report.getErrorMessages().add("Elective group or room is null");
            return report;
        }

        // Calculate total student count from all sections in the elective group
        int totalStudents = booking.getElectiveGroup().getTotalStudentCount();
        int roomCapacity = booking.getRoom().getCapacity();

        if (totalStudents > roomCapacity) {
            String errorMessage = String.format(
                    "Capacity Error: Elective Group '%s' has %d total students, but room '%s' capacity is only %d. " +
                    "Required capacity: %d, Available: %d.",
                    booking.getElectiveGroup().getGroupName(),
                    totalStudents,
                    booking.getRoom().getRoomNumber(),
                    roomCapacity,
                    totalStudents,
                    roomCapacity
            );
            report.addError(errorMessage);
        }

        return report;
    }

    /**
     * Comprehensive validation for elective group bookings
     * Combines conflict checks and capacity validation
     *
     * @param booking the elective group booking request
     * @return ValidationReport with all validation results
     */
    public ValidationReport validateElectiveGroupBooking(Booking booking) {
        ValidationReport report = new ValidationReport();

        // Run all validation checks
        ValidationReport conflictReport = validateElectiveGroupConflict(booking);
        ValidationReport capacityReport = validateElectiveGroupCapacity(booking);

        // Aggregate errors
        report.getErrorMessages().addAll(conflictReport.getErrorMessages());
        report.getErrorMessages().addAll(capacityReport.getErrorMessages());

        // Update feasibility
        if (!report.getErrorMessages().isEmpty()) {
            report.setFeasible(false);
        }

        return report;
    }

    /**
     * Comprehensive validation for standard section bookings when subject might be elective
     *
     * @param booking the section booking request
     * @return ValidationReport with all validation results
     */
    public ValidationReport validateStandardSectionBooking(Booking booking) {
        ValidationReport report = new ValidationReport();

        // Only check for elective conflicts if the subject is an elective
        if (booking.getSubject() != null && booking.getSubject().isElective()) {
            ValidationReport electiveConflictReport = validateSectionElectiveConflict(booking);
            report.getErrorMessages().addAll(electiveConflictReport.getErrorMessages());
        }

        // Update feasibility
        if (!report.getErrorMessages().isEmpty()) {
            report.setFeasible(false);
        }

        return report;
    }
}
