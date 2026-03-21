package com.timetable.backend.controller;

import com.timetable.backend.model.Booking;
import com.timetable.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private BookingService bookingService;

    /**
     * Test endpoint to verify NEW_AUDI access control.
     * Attempts to create a booking for the New Auditorium.
     *
     * @param booking the booking request
     * @return result message indicating success or security violation
     */
    @PostMapping("/test-audi-access")
    public ResponseEntity<Map<String, Object>> testAudiAccess(@RequestBody Booking booking) {
        Map<String, Object> response = new HashMap<>();

        String result = bookingService.createBooking(booking);

        // Check if booking was denied due to security violation
        if (result.contains("Security Violation")) {
            response.put("status", "DENIED");
            response.put("statusCode", 403);
            response.put("message", result);
            response.put("teacher", booking.getTeacher().getName());
            response.put("hasAudiAccess", booking.getTeacher().isHasAudiAccess());
            return ResponseEntity.status(403).body(response);
        }

        // Check for other errors
        if (result.startsWith("Error")) {
            response.put("status", "FAILED");
            response.put("statusCode", 400);
            response.put("message", result);
            return ResponseEntity.badRequest().body(response);
        }

        // Booking succeeded
        response.put("status", "APPROVED");
        response.put("statusCode", 200);
        response.put("message", result);
        response.put("teacher", booking.getTeacher().getName());
        response.put("hasAudiAccess", booking.getTeacher().isHasAudiAccess());
        return ResponseEntity.ok(response);
    }

    /**
     * Check if a teacher has NEW_AUDI access.
     *
     * @param teacherId the teacher ID
     * @return access status for the teacher
     */
    @GetMapping
    ("/teacher/{teacherId}/audi-access")
    public ResponseEntity<Map<String, Object>> checkTeacherAudiAccess(@PathVariable Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        response.put("teacherId", teacherId);
        response.put("facility", "NEW_AUDI");
        response.put("hasAccess", true); // This would be retrieved from the database in a real scenario
        return ResponseEntity.ok(response);
    }
}
