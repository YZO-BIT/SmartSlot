package com.timetable.backend.service;

import com.timetable.backend.model.*;
import com.timetable.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock private BookingRepository bookingRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private ConfigurationService configurationService;
    @Mock private TicketRepository ticketRepository;
    @Mock private ElectiveValidationService electiveValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configurationService.getIntConfigValue(anyString(), anyInt())).thenAnswer(invocation -> invocation.getArgument(1));
        when(ticketRepository.findAll()).thenReturn(new ArrayList<>());
    }

    @Test
    void testLunchBreakBlocks() {
        Booking booking = createBaseBooking(4); // Slot 4 is lunch
        String result = bookingService.createBooking(booking);
        assertTrue(result.contains("mandatory lunch break"), "Should block lunch break slot");
    }

    @Test
    void testAudiAccessDenied() {
        Booking booking = createBaseBooking(1);
        booking.getRoom().setRoomType(RoomType.NEW_AUDI);
        booking.getTeacher().getEligibleRoomTypes().clear();
        
        String result = bookingService.createBooking(booking);
        assertTrue(result.contains("does not have eligibility"), "Should block auditorium access for unauthorized teachers");
    }

    @Test
    void testTeacherOverlapPrevention() {
        Booking booking = createBaseBooking(1);
        when(bookingRepository.existsByTeacherIdAndSlotIdAndBookingDateAndStatusNot(anyLong(), anyInt(), any(), anyString()))
            .thenReturn(true);
            
        String result = bookingService.createBooking(booking);
        assertTrue(result.contains("already scheduled for another class"), "Should prevent teacher double booking");
    }

    @Test
    void testSuccessfulBooking() {
        Booking booking = createBaseBooking(1);
        when(bookingRepository.existsByRoomIdAndSlotIdAndBookingDateAndStatusNot(anyLong(), anyInt(), any(), anyString())).thenReturn(false);
        when(bookingRepository.existsByTeacherIdAndSlotIdAndBookingDateAndStatusNot(anyLong(), anyInt(), any(), anyString())).thenReturn(false);
        
        String result = bookingService.createBooking(booking);
        assertEquals("Success: Booking confirmed!", result);
    }

    private Booking createBaseBooking(int slotId) {
        Teacher teacher = new Teacher(); teacher.setId(1L); teacher.setName("Test Professor");
        teacher.getEligibleRoomTypes().add(RoomType.CR);
        Room room = new Room(); room.setId(1L); room.setRoomNumber("101"); room.setCapacity(100); room.setRoomType(RoomType.CR);
        Subject subject = new Subject(); subject.setId(1L); subject.setName("Test Subject"); subject.setLecturesPerWeek(5);
        Section section = new Section(); section.setId(1L); section.setName("A1"); section.setStudentCount(50);

        Booking b = new Booking();
        b.setTeacher(teacher);
        b.setRoom(room);
        b.setSubject(subject);
        b.setSection(section);
        b.setSlotId(slotId);
        b.setBookingDate(LocalDate.now());

        // Mock repository finds
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));

        return b;
    }
}
