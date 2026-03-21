package com.timetable.backend.controller;

import com.timetable.backend.model.Booking;
import com.timetable.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;

    /**
     * Create a new booking with validation checks
     * @param booking The booking object containing room, teacher, section, slot, and date
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/create")
    public ResponseEntity<String> createBooking(@RequestBody Booking booking) {
        String result = bookingService.createBooking(booking);
        
        if (result.startsWith("Success")) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }

    /**
     * Get all scheduled bookings
     * @return List of all bookings in the database
     */
    @GetMapping("/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
}
