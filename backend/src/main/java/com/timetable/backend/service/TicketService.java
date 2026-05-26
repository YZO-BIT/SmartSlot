package com.timetable.backend.service;

import com.timetable.backend.model.Ticket;
import com.timetable.backend.model.Teacher;
import com.timetable.backend.model.Section;
import com.timetable.backend.repository.TicketRepository;
import com.timetable.backend.repository.TeacherRepository;
import com.timetable.backend.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SectionRepository sectionRepository;

    public Ticket createTicket(Ticket ticket) {
        if (ticket.getTeacher() == null || ticket.getTeacher().getId() == null) {
            throw new RuntimeException("Teacher identification is required!");
        }
        if (ticket.getRequestedSection() == null || ticket.getRequestedSection().getId() == null) {
            throw new RuntimeException("Requested Section is required!");
        }
        if (ticket.getRequestedDate() == null) {
            throw new RuntimeException("Requested Date is required!");
        }
        if (ticket.getRequestedSlotId() == null || ticket.getRequestedSlotId() < 1 || ticket.getRequestedSlotId() > 10) {
            throw new RuntimeException("Requested Slot ID must be between 1 and 10!");
        }
        if (ticket.getReason() == null || ticket.getReason().trim().isEmpty()) {
            throw new RuntimeException("Reason for request is required!");
        }

        // Prevent duplicate pending or approved tickets for the same teacher, date, and slot
        boolean duplicateExists = ticketRepository.findByTeacherId(ticket.getTeacher().getId()).stream()
                .anyMatch(t -> t.getStatus() != Ticket.TicketStatus.REJECTED &&
                        t.getRequestedDate() != null && t.getRequestedDate().equals(ticket.getRequestedDate()) &&
                        t.getRequestedSlotId() != null && t.getRequestedSlotId().equals(ticket.getRequestedSlotId()));
        if (duplicateExists) {
            throw new RuntimeException("An active (Pending or Approved) ticket already exists for this slot and date!");
        }

        Teacher teacher = teacherRepository.findById(ticket.getTeacher().getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Section section = sectionRepository.findById(ticket.getRequestedSection().getId())
                .orElseThrow(() -> new RuntimeException("Section not found"));

        ticket.setTeacher(teacher);
        ticket.setRequestedSection(section);
        ticket.setStatus(Ticket.TicketStatus.PENDING);
        ticket.setCreatedDate(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllPendingTickets() {
        return ticketRepository.findByStatus(Ticket.TicketStatus.PENDING);
    }

    public Ticket approveTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(Ticket.TicketStatus.APPROVED);
        ticket.setApprovedDate(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Ticket rejectTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(Ticket.TicketStatus.REJECTED);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getTicketsByTeacher(Long teacherId) {
        return ticketRepository.findByTeacherId(teacherId);
    }
}
