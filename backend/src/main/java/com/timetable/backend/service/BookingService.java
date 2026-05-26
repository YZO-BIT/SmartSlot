package com.timetable.backend.service;

import com.timetable.backend.model.*;
import com.timetable.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service

public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ElectiveValidationService electiveValidationService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private ElectiveGroupRepository electiveGroupRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;

    public String createBooking(Booking bookingDetails) {
        // Fetch entities to ensure they are managed and we have all fields for
        // validation
        Booking newBooking = new Booking();

        if (bookingDetails.getTeacher() != null && bookingDetails.getTeacher().getId() != null) {
            teacherRepository.findById(bookingDetails.getTeacher().getId())
                    .ifPresent(newBooking::setTeacher);
        }
        if (bookingDetails.getRoom() != null && bookingDetails.getRoom().getId() != null) {
            roomRepository.findById(bookingDetails.getRoom().getId())
                    .ifPresent(newBooking::setRoom);
        }
        if (bookingDetails.getSection() != null && bookingDetails.getSection().getId() != null) {
            sectionRepository.findById(bookingDetails.getSection().getId())
                    .ifPresent(newBooking::setSection);
        }
        if (bookingDetails.getElectiveGroup() != null && bookingDetails.getElectiveGroup().getId() != null) {
            electiveGroupRepository.findById(bookingDetails.getElectiveGroup().getId())
                    .ifPresent(newBooking::setElectiveGroup);
        }
        if (bookingDetails.getSubject() != null && bookingDetails.getSubject().getId() != null) {
            subjectRepository.findById(bookingDetails.getSubject().getId())
                    .ifPresent(newBooking::setSubject);
        }

        newBooking.setSlotId(bookingDetails.getSlotId());
        newBooking.setBookingDate(bookingDetails.getBookingDate());

        if (newBooking.getTeacher() == null || newBooking.getRoom() == null || newBooking.getSubject() == null) {
            return "Error: Teacher, Room, and Subject are mandatory!";
        }

        // TICKET OVERRIDE CHECK
        boolean hasApprovedTicket = ticketRepository.findAll().stream()
                .anyMatch(t -> t.getStatus() == Ticket.TicketStatus.APPROVED &&
                        t.getTeacher().getId().equals(newBooking.getTeacher().getId()) &&
                        t.getRequestedDate() != null && t.getRequestedDate().equals(newBooking.getBookingDate()) &&
                        t.getRequestedSlotId() != null && t.getRequestedSlotId().equals(newBooking.getSlotId()));

        if (hasApprovedTicket) {
            System.out.println("Rule Override active for booking via HOD Ticket!");
        }

        // Mandatory Lunch Break (Configurable)
        int lunchBreakSlot = configurationService.getIntConfigValue("LUNCH_BREAK_SLOT", 4);
        if (newBooking.getSlotId() == lunchBreakSlot && !hasApprovedTicket) {
            return "Error: Slot " + lunchBreakSlot + " is a mandatory lunch break for everyone!";
        }

        // RULE: Room Capacity Check (including joint/grouped lectures in the same room
        // & slot)
        if (newBooking.getSection() != null && newBooking.getRoom() != null) {
            int currentSectionStrength = newBooking.getSection().getStudentCount();

            java.util.List<Booking> activeRoomBookings = bookingRepository.findByRoomIdAndBookingDateOrderBySlotIdAsc(
                    newBooking.getRoom().getId(),
                    newBooking.getBookingDate());

            int combinedStrength = currentSectionStrength;
            for (Booking b : activeRoomBookings) {
                if (b.getSlotId() == newBooking.getSlotId() &&
                        !"CANCELLED".equals(b.getStatus()) &&
                        (newBooking.getId() == null || !b.getId().equals(newBooking.getId()))) {

                    combinedStrength += (b.getSection() != null ? b.getSection().getStudentCount() : 0);
                }
            }

            if (combinedStrength > newBooking.getRoom().getCapacity()) {
                return "Error: Room '" + newBooking.getRoom().getRoomNumber() +
                        "' (Capacity: " + newBooking.getRoom().getCapacity() +
                        ") has insufficient capacity for the combined strength of " +
                        combinedStrength + " students!";
            }
        }

        // SECURITY: Room Type Eligibility (e.g. LAB, NEW_AUDI)
        if (!newBooking.getTeacher().getEligibleRoomTypes().contains(newBooking.getRoom().getRoomType())
                && !hasApprovedTicket) {
            return "Security Violation: Professor '" + newBooking.getTeacher().getName() +
                    "' does not have eligibility for room type: " + newBooking.getRoom().getRoomType().getDescription();
        }

        // SECURITY: Teaching Assignment Enforcement (supports grouped section slots)
        if (newBooking.getSection() != null && newBooking.getSubject() != null) {
            boolean isAssigned = teachingAssignmentRepository.existsByTeacherIdAndSectionIdAndSubjectId(
                    newBooking.getTeacher().getId(),
                    newBooking.getSection().getId(),
                    newBooking.getSubject().getId());

            if (!isAssigned && !hasApprovedTicket && !"ADMIN".equals(newBooking.getTeacher().getRole())) {
                return "Assignment Violation: Professor '" + newBooking.getTeacher().getName() +
                        "' is not officially assigned to teach " + newBooking.getSubject().getName() +
                        " for section " + newBooking.getSection().getName() + ".";
            }
        }

        // LECTURE FREQUENCY VALIDATION (Daily & Weekly limits - Per Section, including grouped slots)
        if (newBooking.getSection() != null && newBooking.getSubject() != null) {
            // 1. Daily Limit: No two classes with the same section and subject on the same day (except LABs)
            if (newBooking.getSubject().getRoomTypeRequirement() != RoomType.LAB) {
                boolean existsToday = bookingRepository.existsBySectionIdAndSubjectIdAndBookingDateAndStatusNot(
                        newBooking.getSection().getId(),
                        newBooking.getSubject().getId(),
                        newBooking.getBookingDate(),
                        "CANCELLED");
                if (existsToday && !hasApprovedTicket) {
                    return "Error: Section " + newBooking.getSection().getName() +
                            " already has a class for " + newBooking.getSubject().getName() + " scheduled today.";
                }
            }

            // 2. Weekly Limit: Count bookings for this section in the same week
            LocalDate startOfWeek = newBooking.getBookingDate()
                    .minusDays(newBooking.getBookingDate().getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            long currentCount = bookingRepository.countBySectionIdAndSubjectIdAndBookingDateBetweenAndStatusNot(
                    newBooking.getSection().getId(),
                    newBooking.getSubject().getId(),
                    startOfWeek,
                    endOfWeek,
                    "CANCELLED");

            System.out.println("DEBUG: Weekly Count for Section " + newBooking.getSection().getId() +
                    " / Subject " + newBooking.getSubject().getId() + " is " + currentCount);

            // Lab occupies 2 slots, regular lecture occupies 1 slot
            int slotsNeeded = (newBooking.getRoom().getRoomType() == RoomType.LAB) ? 2 : 1;
            if (currentCount + slotsNeeded > newBooking.getSubject().getLecturesPerWeek() && !hasApprovedTicket) {
                return "Error: Section " + newBooking.getSection().getName() +
                        " has already reached or would exceed its weekly limit of " +
                        newBooking.getSubject().getLecturesPerWeek() + " lectures for " +
                        newBooking.getSubject().getName() + ".";
            }
        }

        // CONSECUTIVE LAB PERIODS (2-hour slots for labs)
        if (newBooking.getRoom().getRoomType() == RoomType.LAB) {
            int labDuration = configurationService.getIntConfigValue("LAB_DURATION", 2);
            if (labDuration > 1) {
                // Check if this is the start of a block (Odd slots: 1, 3, 5, 7, 9)
                if (newBooking.getSlotId() % 2 == 0 && !hasApprovedTicket) {
                    return "Error: Lab sessions must start on an odd slot (1, 3, 5, 7, 9) for a 2-hour continuous block.";
                }
                // Verify the next slot is also free
                if (bookingRepository.existsByRoomIdAndSlotIdAndBookingDateAndStatusNot(
                        newBooking.getRoom().getId(),
                        newBooking.getSlotId() + 1,
                        newBooking.getBookingDate(),
                        "CANCELLED") && !hasApprovedTicket) {
                    return "Error: Lab requires 2 consecutive available slots. Slot " + (newBooking.getSlotId() + 1)
                            + " is already occupied!";
                }
            }
        }

        // Standard validation (keep original as fallback)
        // ... (rest of the checks)

        // RULE: Room Conflict (Is the room already booked and ACTIVE by another
        // lecture?)
        java.util.List<Booking> roomBookings = bookingRepository.findByRoomIdAndBookingDateOrderBySlotIdAsc(
                newBooking.getRoom().getId(),
                newBooking.getBookingDate());
        boolean hasRoomConflict = roomBookings.stream()
                .anyMatch(b -> b.getSlotId() == newBooking.getSlotId() &&
                        !"CANCELLED".equals(b.getStatus()) &&
                        !(b.getTeacher().getId().equals(newBooking.getTeacher().getId()) &&
                                b.getSubject().getId().equals(newBooking.getSubject().getId())));
        if (hasRoomConflict) {
            return "Error: This room is already booked for this slot!";
        }

        // RULE: Teacher Conflict (Is the teacher already teaching another ACTIVE class
        // in a different room/subject?)
        java.util.List<Booking> teacherBookings = bookingRepository.findByTeacherIdAndBookingDateOrderBySlotIdAsc(
                newBooking.getTeacher().getId(),
                newBooking.getBookingDate());
        boolean hasTeacherConflict = teacherBookings.stream()
                .anyMatch(b -> b.getSlotId() == newBooking.getSlotId() &&
                        !"CANCELLED".equals(b.getStatus()) &&
                        !(b.getRoom().getId().equals(newBooking.getRoom().getId()) &&
                                b.getSubject().getId().equals(newBooking.getSubject().getId())));
        if (hasTeacherConflict) {
            return "Error: You are already scheduled for another class during this slot!";
        }

        // RULE: Teacher Conflict for slotId + 1 (for consecutive Lab slots)
        if (newBooking.getRoom().getRoomType() == RoomType.LAB) {
            int nextSlotId = newBooking.getSlotId() + 1;
            boolean hasNextTeacherConflict = teacherBookings.stream()
                    .anyMatch(b -> b.getSlotId() == nextSlotId &&
                            !"CANCELLED".equals(b.getStatus()) &&
                            !(b.getRoom().getId().equals(newBooking.getRoom().getId()) &&
                                    b.getSubject().getId().equals(newBooking.getSubject().getId())));
            if (hasNextTeacherConflict) {
                return "Error: You are already scheduled for another class during the consecutive slot (Slot "
                        + nextSlotId + ")!";
            }
        }

        // RULE: Section Conflict (Is the section already in another ACTIVE class?)
        if (newBooking.getSection() != null && bookingRepository.existsBySectionIdAndSlotIdAndBookingDateAndStatusNot(
                newBooking.getSection().getId(),
                newBooking.getSlotId(),
                newBooking.getBookingDate(),
                "CANCELLED")) {
            return "Error: Section '" + newBooking.getSection().getName()
                    + "' is already scheduled for another class during this slot!";
        }

        // RULE: Section Conflict for slotId + 1 (for consecutive Lab slots)
        if (newBooking.getRoom().getRoomType() == RoomType.LAB) {
            int nextSlotId = newBooking.getSlotId() + 1;
            if (newBooking.getSection() != null
                    && bookingRepository.existsBySectionIdAndSlotIdAndBookingDateAndStatusNot(
                            newBooking.getSection().getId(),
                            nextSlotId,
                            newBooking.getBookingDate(),
                            "CANCELLED")) {
                return "Error: Section '" + newBooking.getSection().getName()
                        + "' is already scheduled for another class during the consecutive slot (Slot " + nextSlotId
                        + ")!";
            }
        }

        // RULE: The 2-Class Limit (Has the teacher already taught 2 consecutive classes
        // before this slot?)
        if (checkTeacherNeedsBreak(newBooking)) {
            return "Error: You have taught 2 consecutive classes. Please take a 1-hour break!";
        }

        // If all checks pass, save the booking
        bookingRepository.save(newBooking);

        // Automatically book the second slot for lab sessions (slotId + 1)
        if (newBooking.getRoom().getRoomType() == RoomType.LAB) {
            int nextSlotId = newBooking.getSlotId() + 1;
            boolean alreadyBooked = bookingRepository.existsBySectionIdAndSlotIdAndBookingDateAndStatusNot(
                    newBooking.getSection().getId(),
                    nextSlotId,
                    newBooking.getBookingDate(),
                    "CANCELLED");
            if (!alreadyBooked) {
                Booking nextBooking = new Booking();
                nextBooking.setTeacher(newBooking.getTeacher());
                nextBooking.setRoom(newBooking.getRoom());
                nextBooking.setSection(newBooking.getSection());
                nextBooking.setSubject(newBooking.getSubject());
                nextBooking.setSlotId(nextSlotId);
                nextBooking.setBookingDate(newBooking.getBookingDate());
                nextBooking.setStatus(newBooking.getStatus() != null ? newBooking.getStatus() : "CONFIRMED");
                bookingRepository.save(nextBooking);
            }
        }
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
            if (b.getSlotId() == currentSlot - 1)
                hasPrev1 = true;
            if (b.getSlotId() == currentSlot - 2)
                hasPrev2 = true;
        }
        return hasPrev1 && hasPrev2; // Teacher has taught 2 consecutive classes before this slot return true.
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByTeacher(Long teacherId, java.time.LocalDate date) {
        return bookingRepository.findByTeacherIdAndBookingDateOrderBySlotIdAsc(teacherId, date);
    }

    public List<Booking> getBookingsByRoom(Long roomId, java.time.LocalDate date) {
        return bookingRepository.findByRoomIdAndBookingDateOrderBySlotIdAsc(roomId, date);
    }

    public List<Booking> getBookingsBySection(Long sectionId, java.time.LocalDate date) {
        return bookingRepository.findBySectionIdAndBookingDateOrderBySlotIdAsc(sectionId, date);
    }

    public List<Booking> getWeeklyBookingsByTeacher(Long teacherId, java.time.LocalDate start,
            java.time.LocalDate end) {
        return bookingRepository.findByTeacherIdAndBookingDateBetweenOrderByBookingDateAscSlotIdAsc(teacherId, start,
                end);
    }

    public List<Booking> getWeeklyBookingsByRoom(Long roomId, java.time.LocalDate start, java.time.LocalDate end) {
        return bookingRepository.findByRoomIdAndBookingDateBetweenOrderByBookingDateAscSlotIdAsc(roomId, start, end);
    }

    public List<Booking> getWeeklyBookingsBySection(Long sectionId, java.time.LocalDate start,
            java.time.LocalDate end) {
        return bookingRepository.findBySectionIdAndBookingDateBetweenOrderByBookingDateAscSlotIdAsc(sectionId, start,
                end);
    }

    public java.util.Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public void deleteBooking(Long id) {
        bookingRepository.findById(id).ifPresent(booking -> {
            // Also delete partner lab slot if applicable
            if (booking.getRoom() != null && booking.getRoom().getRoomType() == RoomType.LAB) {
                int partnerSlot = (booking.getSlotId() % 2 == 0) ? booking.getSlotId() - 1 : booking.getSlotId() + 1;
                findPartnerBooking(booking, partnerSlot).ifPresent(partner -> {
                    bookingRepository.deleteById(partner.getId());
                });
            }
            bookingRepository.deleteById(id);
        });
    }

    public java.util.Optional<Booking> findPartnerBooking(Booking booking, int partnerSlotId) {
        if (booking.getRoom() == null || booking.getSection() == null || booking.getTeacher() == null) {
            return java.util.Optional.empty();
        }
        return bookingRepository.findByRoomIdAndBookingDateOrderBySlotIdAsc(
                booking.getRoom().getId(),
                booking.getBookingDate()).stream()
                .filter(b -> b.getSlotId() == partnerSlotId &&
                        !"CANCELLED".equals(b.getStatus()) &&
                        b.getTeacher().getId().equals(booking.getTeacher().getId()) &&
                        b.getSection().getId().equals(booking.getSection().getId()) &&
                        b.getSubject().getId().equals(booking.getSubject().getId()))
                .findFirst();
    }

    public boolean existsById(Long id) {
        return bookingRepository.existsById(id);
    }

    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public String updateBooking(Long id, Booking bookingDetails) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    if (bookingDetails.getTeacher() != null && bookingDetails.getTeacher().getId() != null) {
                        teacherRepository.findById(bookingDetails.getTeacher().getId())
                                .ifPresent(booking::setTeacher);
                    }
                    if (bookingDetails.getRoom() != null && bookingDetails.getRoom().getId() != null) {
                        roomRepository.findById(bookingDetails.getRoom().getId())
                                .ifPresent(booking::setRoom);
                    }
                    if (bookingDetails.getSection() != null && bookingDetails.getSection().getId() != null) {
                        sectionRepository.findById(bookingDetails.getSection().getId())
                                .ifPresent(booking::setSection);
                    } else {
                        booking.setSection(null);
                    }
                    if (bookingDetails.getElectiveGroup() != null
                            && bookingDetails.getElectiveGroup().getId() != null) {
                        electiveGroupRepository.findById(bookingDetails.getElectiveGroup().getId())
                                .ifPresent(booking::setElectiveGroup);
                    } else {
                        booking.setElectiveGroup(null);
                    }
                    if (bookingDetails.getSubject() != null && bookingDetails.getSubject().getId() != null) {
                        subjectRepository.findById(bookingDetails.getSubject().getId())
                                .ifPresent(booking::setSubject);
                    }

                    booking.setSlotId(bookingDetails.getSlotId());
                    booking.setBookingDate(bookingDetails.getBookingDate());
                    bookingRepository.save(booking);
                    return "Success: Booking updated!";
                })
                .orElse("Error: Booking not found");
    }

    public java.util.Map<String, Long> getDashboardStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("teachers", teacherRepository.count());
        stats.put("rooms", roomRepository.count());
        stats.put("sections", sectionRepository.count());
        stats.put("bookings", bookingRepository.count());
        return stats;
    }

    public java.util.Map<String, Object> getTeacherDashboardStats(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        if (teacher == null)
            return stats;

        LocalDate today = LocalDate.now();
        LocalDate mon = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sat = mon.plusDays(5);

        long todayCount = bookingRepository.findByTeacherIdAndBookingDateOrderBySlotIdAsc(teacherId, today).size();
        long weekCount = bookingRepository
                .findByTeacherIdAndBookingDateBetweenOrderByBookingDateAscSlotIdAsc(teacherId, mon, sat).size();
        long pending = bookingRepository.findAll().stream()
                .filter(b -> b.getTeacher().getId().equals(teacherId) && "PENDING_CANCEL".equals(b.getStatus()))
                .count();

        stats.put("todayLectures", todayCount);
        stats.put("totalLectures", weekCount);
        stats.put("pendingRequests", pending);
        stats.put("department", teacher.getDepartment());

        return stats;
    }
}