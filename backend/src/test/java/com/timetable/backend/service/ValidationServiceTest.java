package com.timetable.backend.service;

import com.timetable.backend.dto.ValidationReport;
import com.timetable.backend.model.*;
import com.timetable.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ValidationServiceTest {

    @Mock private RoomRepository roomRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private ValidationService validationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRoomAvailability_Success() {
        Subject math = new Subject();
        math.setName("Math");
        math.setRoomTypeRequirement(RoomType.CR);

        Section a1 = new Section();
        a1.setSubjects(new HashSet<>(Collections.singletonList(math)));

        Room room = new Room();
        room.setRoomType(RoomType.CR);

        when(subjectRepository.findAll()).thenReturn(Collections.singletonList(math));
        when(sectionRepository.findAll()).thenReturn(Collections.singletonList(a1));
        when(roomRepository.findAll()).thenReturn(Collections.singletonList(room));

        ValidationReport report = validationService.validateRoomAvailability();
        assertTrue(report.isFeasible(), "Report should be feasible");
    }

    @Test
    public void testRoomAvailability_Fail() {
        Subject labSubject = new Subject();
        labSubject.setName("Physics Lab");
        labSubject.setRoomTypeRequirement(RoomType.LAB);

        Section a1 = new Section();
        a1.setSubjects(new HashSet<>(Collections.singletonList(labSubject)));

        Room classRoom = new Room();
        classRoom.setRoomType(RoomType.CR); // No Lab available

        when(subjectRepository.findAll()).thenReturn(Collections.singletonList(labSubject));
        when(sectionRepository.findAll()).thenReturn(Collections.singletonList(a1));
        when(roomRepository.findAll()).thenReturn(Collections.singletonList(classRoom));

        ValidationReport report = validationService.validateRoomAvailability();
        assertFalse(report.isFeasible(), "Report should be infeasible when room type is missing");
        assertTrue(report.getErrorMessages().size() > 0);
    }

    @Test
    public void testSectionCapacity_Exceeded() {
        Subject s1 = new Subject(); s1.setLecturesPerWeek(25);
        Subject s2 = new Subject(); s2.setLecturesPerWeek(20); // total 45 > 40 (MAX_WEEKLY_SLOTS)

        Section a1 = new Section();
        a1.setName("A1");
        a1.setSubjects(new HashSet<>(Arrays.asList(s1, s2)));

        when(sectionRepository.findAll()).thenReturn(Collections.singletonList(a1));

        ValidationReport report = validationService.validateSectionCapacity();
        assertFalse(report.isFeasible(), "Section exceeding weekly limit should be infeasible");
        assertTrue(report.getErrorMessages().size() > 0);
        assertTrue(report.getErrorMessages().get(0).toLowerCase().contains("slots per week"));
    }
}
