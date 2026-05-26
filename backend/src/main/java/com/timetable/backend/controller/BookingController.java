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
     * 
     * @param booking The booking object containing room, teacher, section, slot,
     *                and date
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
     * 
     * @return List of all bookings in the database
     */
    @GetMapping("/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get filtered bookings based on type (teacher, room, section) and id
     * 
     * @param type The type of filter: 'teacher', 'room', or 'section'
     * @param id   The ID of the entity
     * @param date Optional date (defaults to today)
     * @return List of bookings for that entity on that date
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Booking>> getBookingsByFilter(
            @RequestParam String type,
            @RequestParam Long id,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {

        java.time.LocalDate targetDate = (date != null) ? date : java.time.LocalDate.now();
        List<Booking> results;

        boolean isWeekly = (startDate != null && endDate != null);

        switch (type.toLowerCase()) {
            case "teacher":
                results = isWeekly ? bookingService.getWeeklyBookingsByTeacher(id, startDate, endDate)
                        : bookingService.getBookingsByTeacher(id, targetDate);
                break;
            case "room":
                results = isWeekly ? bookingService.getWeeklyBookingsByRoom(id, startDate, endDate)
                        : bookingService.getBookingsByRoom(id, targetDate);
                break;
            case "section":
                results = isWeekly ? bookingService.getWeeklyBookingsBySection(id, startDate, endDate)
                        : bookingService.getBookingsBySection(id, targetDate);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateBooking(@PathVariable Long id, @RequestBody Booking booking) {
        String result = bookingService.updateBooking(id, booking);
        if (result.startsWith("Success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }

    // --- CANCELLATION REQUESTS ---

    @PostMapping("/{id}/request-cancel")
    public ResponseEntity<String> requestCancel(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(booking -> {
                    booking.setStatus("PENDING_CANCEL");
                    bookingService.saveBooking(booking);

                    // Also request cancel for partner lab slot if applicable
                    if (booking.getRoom() != null
                            && booking.getRoom().getRoomType() == com.timetable.backend.model.RoomType.LAB) {
                        int partnerSlot = (booking.getSlotId() % 2 == 0) ? booking.getSlotId() - 1
                                : booking.getSlotId() + 1;
                        bookingService.findPartnerBooking(booking, partnerSlot).ifPresent(partner -> {
                            partner.setStatus("PENDING_CANCEL");
                            bookingService.saveBooking(partner);
                        });
                    }

                    return ResponseEntity.ok("Cancellation request submitted.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve-cancel")
    public ResponseEntity<String> approveCancel(@PathVariable Long id) {
        java.util.Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            // Also delete partner lab slot if applicable
            if (booking.getRoom() != null
                    && booking.getRoom().getRoomType() == com.timetable.backend.model.RoomType.LAB) {
                int partnerSlot = (booking.getSlotId() % 2 == 0) ? booking.getSlotId() - 1 : booking.getSlotId() + 1;
                bookingService.findPartnerBooking(booking, partnerSlot).ifPresent(partner -> {
                    bookingService.deleteBooking(partner.getId());
                });
            }

            bookingService.deleteBooking(id);
            return ResponseEntity.ok("Booking cancelled and slot released.");
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/reject-cancel")
    public ResponseEntity<String> rejectCancel(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(booking -> {
                    booking.setStatus("CONFIRMED");
                    bookingService.saveBooking(booking);

                    // Also reject cancel for partner lab slot if applicable
                    if (booking.getRoom() != null
                            && booking.getRoom().getRoomType() == com.timetable.backend.model.RoomType.LAB) {
                        int partnerSlot = (booking.getSlotId() % 2 == 0) ? booking.getSlotId() - 1
                                : booking.getSlotId() + 1;
                        bookingService.findPartnerBooking(booking, partnerSlot).ifPresent(partner -> {
                            partner.setStatus("CONFIRMED");
                            bookingService.saveBooking(partner);
                        });
                    }

                    return ResponseEntity.ok("Cancellation request rejected.");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
