package com.timetable.backend.service;

import com.timetable.backend.dto.ValidationReport;
import com.timetable.backend.model.Booking;
import com.timetable.backend.model.RoomType;
import com.timetable.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service

public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ElectiveValidationService electiveValidationService;

    public String createBooking(Booking newBooking) {
        
        // Mandatory Lunch Break (12:00 - 1:00 PM is a mandatory break for everyone)
        if (newBooking.getSlotId() == 4) {
            return "Error: 12:00 - 1:00 PM is a mandatory lunch break for everyone!";
        }

        // SECURITY: NEW_AUDI Access Control (Premium Facility Restricted Access)
        if (newBooking.getRoom() != null && newBooking.getRoom().getRoomType() == RoomType.NEW_AUDI) {
            if (!newBooking.getTeacher().isHasAudiAccess()) {
                return "Security Violation: Professor '" + newBooking.getTeacher().getName() + 
                       "' does not have clearance for the New Auditorium.";
            }
        }

        // TASK 2 & 3: Elective Group Validation
        if (newBooking.isElectiveBooking()) {
            // Condition A: Check if any section in the elective group is already busy
            ValidationReport conflictReport = electiveValidationService.validateElectiveGroupConflict(newBooking);
            if (!conflictReport.isFeasible()) {
                return String.join(" | ", conflictReport.getErrorMessages());
            }

            // Task 3: Dynamic Room Capacity Check
            ValidationReport capacityReport = electiveValidationService.validateElectiveGroupCapacity(newBooking);
            if (!capacityReport.isFeasible()) {
                return String.join(" | ", capacityReport.getErrorMessages());
            }
        }

        // Condition B: Check if section belongs to an elective group already booked
        if (newBooking.isSectionBooking()) {
            ValidationReport electiveConflictReport = electiveValidationService.validateStandardSectionBooking(newBooking);
            if (!electiveConflictReport.isFeasible()) {
                return String.join(" | ", electiveConflictReport.getErrorMessages());
            }
        }

        // Room Conflict (Is the room already booked for this slot?)
        if (bookingRepository.existsByRoomIdAndSlotIdAndBookingDate(
                newBooking.getRoom().getId(), 
                newBooking.getSlotId(), 
                newBooking.getBookingDate())) {
            return "Error: This room is already booked for this slot!";
        }

        // RULE: Teacher Conflict (Is the teacher already teaching another class?)
        if (bookingRepository.existsByTeacherIdAndSlotIdAndBookingDate(
                newBooking.getTeacher().getId(), 
                newBooking.getSlotId(), 
                newBooking.getBookingDate())) {
            return "Error: You are already scheduled for another class during this slot!";
        }

        // RULE: The 2-Class Limit (Has the teacher already taught 2 consecutive classes before this slot?)
        if (checkTeacherNeedsBreak(newBooking)) {
            return "Error: You have taught 2 consecutive classes. Please take a 1-hour break!";
        }

        // If all checks pass, save the booking
        bookingRepository.save(newBooking);
        return "Success: Booking confirmed!";
    }

    private boolean checkTeacherNeedsBreak(Booking newBooking) {
        List<Booking> todayBookings = bookingRepository.findByTeacherIdAndBookingDateOrderBySlotIdAsc(
                newBooking.getTeacher().getId(), 
                newBooking.getBookingDate());

        int currentSlot = newBooking.getSlotId();
        boolean hasPrev1 = false;
        boolean hasPrev2 = false;

        for (Booking b : todayBookings) {
            if (b.getSlotId() == currentSlot - 1) hasPrev1 = true;
            if (b.getSlotId() == currentSlot - 2) hasPrev2 = true;
        }
        return hasPrev1 && hasPrev2; // Teacher has taught 2 consecutive classes before this slot return true.
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
}