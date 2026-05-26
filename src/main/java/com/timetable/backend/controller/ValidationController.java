package com.timetable.backend.controller;

import com.timetable.backend.dto.ValidationReport;
import com.timetable.backend.model.RoomType;
import com.timetable.backend.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {

    @Autowired
    private ValidationService validationService;

    /**
     * Pre-flight validation for the genetic algorithm.
     * Checks if all subjects have required room types available.
     *
     * @return ValidationReport with feasibility status and error messages
     */
    @GetMapping("/preflight")
    public ResponseEntity<ValidationReport> validateBeforeGA() {
        ValidationReport report = validationService.validateRoomAvailability();
        return ResponseEntity.ok(report);
    }

    /**
     * Check if rooms of a specific type are available.
     *
     * @param roomType the room type to check (CR, LAB, LT, NEW_AUDI)
     * @return ValidationReport indicating availability
     */
    @GetMapping("/room-type/{roomType}")
    public ResponseEntity<ValidationReport> checkRoomTypeAvailability(@PathVariable String roomType) {
        try {
            RoomType type = RoomType.valueOf(roomType.toUpperCase());
            ValidationReport report = new ValidationReport();

            if (validationService.isRoomTypeAvailable(type)) {
                report.setFeasible(true);
                long count = validationService.countRoomsByType(type);
                report.getErrorMessages().add(
                        String.format("✓ Room type '%s' is available. Total rooms: %d", type.getDescription(), count)
                );
            } else {
                report.addError(String.format("✗ Room type '%s' is NOT available in database.", type.getDescription()));
            }

            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            ValidationReport report = new ValidationReport();
            report.addError("Invalid room type. Available types: CR, LAB, LT, NEW_AUDI");
            return ResponseEntity.badRequest().body(report);
        }
    }

    /**
     * Validates teacher expertise for subject assignments (Task 1).
     * Checks if teachers assigned to subjects are qualified (have expertise).
     *
     * @return ValidationReport with expertise mismatch errors
     */
    @GetMapping("/teacher-expertise")
    public ResponseEntity<ValidationReport> validateTeacherExpertise() {
        ValidationReport report = validationService.validateTeacherExpertise();
        return ResponseEntity.ok(report);
    }

    /**
     * Validates section capacity for weekly slots (Task 2).
     * Checks if sections have schedule conflicts due to slot overflow.
     *
     * @return ValidationReport with capacity overflow errors
     */
    @GetMapping("/section-capacity")
    public ResponseEntity<ValidationReport> validateSectionCapacity() {
        ValidationReport report = validationService.validateSectionCapacity();
        return ResponseEntity.ok(report);
    }

    /**
     * Comprehensive validation combining all consistency checks.
     * Prevents Ghost Bookings by validating all data dependencies.
     *
     * @return ValidationReport with all validation results
     */
    @GetMapping("/complete-validation")
    public ResponseEntity<ValidationReport> validateAllDataConsistency() {
        ValidationReport report = validationService.validateAllDataConsistency();
        return ResponseEntity.ok(report);
    }
}
