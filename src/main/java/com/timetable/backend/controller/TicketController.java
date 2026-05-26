package com.timetable.backend.controller;

import com.timetable.backend.model.Ticket;
import com.timetable.backend.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin("*")
public class TicketController {
    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        try {
            return ResponseEntity.ok(ticketService.createTicket(ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new com.timetable.backend.dto.LoginResponse(false, e.getMessage(), null, null, null, null, null));
        }
    }

    @GetMapping
    public ResponseEntity<?> getPendingTickets() {
        try {
            return ResponseEntity.ok(ticketService.getAllPendingTickets());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveTicket(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ticketService.approveTicket(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectTicket(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ticketService.rejectTicket(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getTeacherTickets(@PathVariable Long teacherId) {
        try {
            return ResponseEntity.ok(ticketService.getTicketsByTeacher(teacherId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
