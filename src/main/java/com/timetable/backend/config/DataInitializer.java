package com.timetable.backend.config;

import com.timetable.backend.model.*;
import com.timetable.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.timetable.backend.service.TeacherService;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ElectiveGroupRepository electiveGroupRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * CommandLineRunner bean to initialize database with sample data
     */
    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            // Drop old legacy section_id column if it exists in teaching_assignments
            try {
                jdbcTemplate.execute("ALTER TABLE teaching_assignments DROP COLUMN IF EXISTS section_id CASCADE");
                System.out.println("✓ Successfully dropped legacy column teaching_assignments.section_id!");
            } catch (Exception e) {
                System.err.println("Could not drop legacy column: " + e.getMessage());
            }

            // Ensure Sample Teachers exist with credentials
            Optional<Teacher> sharmaOpt = teacherService.findByEmail("sharma@college.edu");
            Teacher teacher1 = sharmaOpt.orElse(new Teacher());
            if (sharmaOpt.isEmpty()) {
                teacher1.setName("Dr. Sharma");
                teacher1.setUsername("sharma_admin");
                teacher1.setEmail("sharma@college.edu");
                teacher1.setPassword("admin123");
                teacher1.setDepartment("Computer Science");
            }
            // Always ensure Admin role and approval
            teacher1.setRole("ADMIN");
            teacher1.setApproved(true);
            teacher1.getEligibleRoomTypes().add(RoomType.NEW_AUDI);
            teacher1.getEligibleRoomTypes().add(RoomType.CR);
            teacher1.getEligibleRoomTypes().add(RoomType.LT);
            teacherService.saveTeacher(teacher1);
            System.out.println("✓ Dr. Sharma credential verified/initialized!");

            Optional<Teacher> vermaOpt = teacherService.findByEmail("verma@college.edu");
            if (vermaOpt.isEmpty()) {
                Teacher teacher2 = new Teacher();
                teacher2.setName("Prof. Verma");
                teacher2.setUsername("verma_admin");
                teacher2.setEmail("verma@college.edu");
                teacher2.setPassword("admin123");
                teacher2.setDepartment("Computer Science");
                teacher2.setRole("ADMIN");
                teacher2.setApproved(true);
                teacher2.getEligibleRoomTypes().add(RoomType.CR);
                teacher2.getEligibleRoomTypes().add(RoomType.LT);
                teacherRepository.save(teacher2);
                System.out.println("✓ Prof. Verma credential initialized!");
            }

            /*
             * if (sectionRepository.count() == 0) {
             * ...
             * }
             */

            /*
             * if (bookingRepository.count() == 0) {
             * ...
             * }
             * if (electiveGroupRepository.count() == 0) { ... }
             */

            // Initialize Configurations
            if (configurationRepository.count() == 0) {
                com.timetable.backend.model.Configuration lunch = new com.timetable.backend.model.Configuration();
                lunch.setConfigKey("LUNCH_BREAK_SLOT");
                lunch.setConfigValue("4");
                lunch.setDescription("The slot ID designated for mandatory lunch break.");
                configurationRepository.save(lunch);

                com.timetable.backend.model.Configuration labLen = new com.timetable.backend.model.Configuration();
                labLen.setConfigKey("LAB_DURATION");
                labLen.setConfigValue("2");
                labLen.setDescription("Number of consecutive slots required for Lab subjects.");
                configurationRepository.save(labLen);
                System.out.println("✓ Institution configurations seeded!");
            }

            System.out.println("✓ Real Data mode active: Skipping sample entity seeding.");
            System.out.println("✓ Database initialization completed!");
        };
    }
}
