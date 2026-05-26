package com.timetable.backend.controller;

import com.timetable.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        return ResponseEntity.ok(bookingService.getDashboardStats());
    }

    @GetMapping("/teacher/{id}/stats")
    public ResponseEntity<Map<String, Object>> getTeacherStats(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getTeacherDashboardStats(id));
    }
}
