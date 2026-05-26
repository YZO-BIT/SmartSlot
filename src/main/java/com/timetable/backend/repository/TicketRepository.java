package com.timetable.backend.repository;

import com.timetable.backend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    List<Ticket> findByTeacherId(Long teacherId);
}
