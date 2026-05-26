package com.timetable.backend.service;

import com.timetable.backend.dto.ValidationReport;
import com.timetable.backend.model.*;
import com.timetable.backend.repository.ElectiveGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class ElectiveValidationServiceTest {

    @Mock
    private ElectiveGroupRepository electiveGroupRepository;

    @InjectMocks
    private ElectiveValidationService electiveValidationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testElectiveGroupCapacity_TooSmall() {
        ElectiveGroup group = new ElectiveGroup();
        Set<Section> sections = new HashSet<>();
        
        Section s1 = new Section(); s1.setId(1L); s1.setStudentCount(60);
        Section s2 = new Section(); s2.setId(2L); s2.setStudentCount(50);
        sections.add(s1);
        sections.add(s2);
        group.setSections(sections);

        Room room = new Room();
        room.setCapacity(100); // 100 < 110 (60+50)

        Booking booking = new Booking();
        booking.setElectiveGroup(group);
        booking.setRoom(room);

        ValidationReport report = electiveValidationService.validateElectiveGroupCapacity(booking);
        
        assertFalse(report.isFeasible(), "Report should be infeasible when room is too small");
        assertTrue(report.getErrorMessages().size() > 0);
        assertTrue(report.getErrorMessages().get(0).contains("Capacity Error"));
    }

    @Test
    public void testElectiveGroupCapacity_Fits() {
        ElectiveGroup group = new ElectiveGroup();
        Set<Section> sections = new HashSet<>();
        Section s1 = new Section(); s1.setId(1L); s1.setStudentCount(40);
        sections.add(s1);
        group.setSections(sections);

        Room room = new Room();
        room.setCapacity(50);

        Booking booking = new Booking();
        booking.setElectiveGroup(group);
        booking.setRoom(room);

        ValidationReport report = electiveValidationService.validateElectiveGroupCapacity(booking);
        assertTrue(report.isFeasible(), "Report should be feasible when room is large enough");
    }

    @Test
    public void testElectiveGroupConflict_BusySection() {
        ElectiveGroup group = new ElectiveGroup();
        group.setId(1L);

        Booking booking = new Booking();
        booking.setElectiveGroup(group);
        booking.setSlotId(1);
        booking.setBookingDate(LocalDate.now());

        List<Object> conflicts = new ArrayList<>();
        Section s = new Section(); s.setName("Busy Section");
        conflicts.add(s);

        when(electiveGroupRepository.findConflictingSectionsInGroup(any(), anyInt(), any())).thenReturn(conflicts);

        ValidationReport report = electiveValidationService.validateElectiveGroupConflict(booking);
        assertFalse(report.isFeasible(), "Report should be infeasible if a section is busy");
    }
}
