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

@Configuration
@Profile("!test")
public class DataInitializer {

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

    /**
     * CommandLineRunner bean to initialize database with sample data
     */
    @Bean
    public CommandLineRunner initializeDatabase() {
        return args -> {
            // Only initialize if database is empty
            if (roomRepository.count() == 0) {
                // Create and save Rooms with room types
                Room room1 = new Room();
                room1.setRoomNumber("CR 101");
                room1.setCapacity(70);
                room1.setRoomType(RoomType.CR);
                roomRepository.save(room1);

                Room room2 = new Room();
                room2.setRoomNumber("CR 102");
                room2.setCapacity(70);
                room2.setRoomType(RoomType.CR);
                roomRepository.save(room2);

                Room room3 = new Room();
                room3.setRoomNumber("LT-101");
                room3.setCapacity(200);
                room3.setRoomType(RoomType.LT);
                roomRepository.save(room3);

                Room room4 = new Room();
                room4.setRoomNumber("LAB-01");
                room4.setCapacity(50);
                room4.setRoomType(RoomType.LAB);
                roomRepository.save(room4);

                Room room5 = new Room();
                room5.setRoomNumber("AUDI-01");
                room5.setCapacity(500);
                room5.setRoomType(RoomType.NEW_AUDI);
                roomRepository.save(room5);

                System.out.println("✓ 5 Rooms initialized successfully!");
            }

            if (teacherRepository.count() == 0) {
                // Create and save Teachers
                Teacher teacher1 = new Teacher();
                teacher1.setName("Dr. Sharma");
                teacher1.setDepartment("Computer Science");
                teacher1.setHasAudiAccess(true); // Senior Professor with NEW_AUDI clearance
                teacherRepository.save(teacher1);

                Teacher teacher2 = new Teacher();
                teacher2.setName("Prof. Verma");
                teacher2.setDepartment("Computer Science");
                teacher2.setHasAudiAccess(false); // Regular professor without NEW_AUDI clearance
                teacherRepository.save(teacher2);

                System.out.println("✓ 2 Teachers initialized successfully!");
                System.out.println("  - Dr. Sharma: NEW_AUDI Access GRANTED ✓");
                System.out.println("  - Prof. Verma: NEW_AUDI Access DENIED ✗");
            }

            if (sectionRepository.count() == 0) {
                // Create and save Sections with student counts
                Section section1 = new Section();
                section1.setName("A1");
                section1.setBatchYear(2024);
                section1.setStudentCount(68);
                sectionRepository.save(section1);

                Section section2 = new Section();
                section2.setName("A2");
                section2.setBatchYear(2024);
                section2.setStudentCount(70);
                sectionRepository.save(section2);

                Section section3 = new Section();
                section3.setName("B1");
                section3.setBatchYear(2024);
                section3.setStudentCount(68);
                sectionRepository.save(section3);

                Section section4 = new Section();
                section4.setName("B2");
                section4.setBatchYear(2024);
                section4.setStudentCount(72);
                sectionRepository.save(section4);

                System.out.println("✓ 4 Sections initialized successfully!");
                System.out.println("  - A1: 68 students");
                System.out.println("  - A2: 70 students");
                System.out.println("  - B1: 68 students");
                System.out.println("  - B2: 72 students");
            }

            if (subjectRepository.count() == 0) {
                // Create and save Subjects
                Subject subject1 = new Subject();
                subject1.setName("Data Structures");
                subject1.setRoomTypeRequirement(RoomType.CR);
                subject1.setDescription("Study of organizing and managing data");
                subject1.setLecturesPerWeek(3);
                subject1 = subjectRepository.save(subject1);

                Subject subject2 = new Subject();
                subject2.setName("Database Systems Lab");
                subject2.setRoomTypeRequirement(RoomType.LAB);
                subject2.setDescription("Practical database management");
                subject2.setLecturesPerWeek(4);
                subject2 = subjectRepository.save(subject2);

                Subject subject3 = new Subject();
                subject3.setName("Operating Systems");
                subject3.setRoomTypeRequirement(RoomType.LT);
                subject3.setDescription("Comprehensive lecture on OS concepts");
                subject3.setLecturesPerWeek(3);
                subject3 = subjectRepository.save(subject3);

                Subject subject4 = new Subject();
                subject4.setName("Annual Seminar");
                subject4.setRoomTypeRequirement(RoomType.NEW_AUDI);
                subject4.setDescription("University-wide seminar");
                subject4.setLecturesPerWeek(2);
                subject4 = subjectRepository.save(subject4);

                System.out.println("✓ 4 Subjects initialized successfully!");

                // Add teacher expertise - fetch fresh teachers within the transaction
                List<Teacher> teachers = teacherRepository.findAll();
                if (teachers.size() >= 2) {
                    Teacher teacher1 = teachers.get(0);
                    Teacher teacher2 = teachers.get(1);

                    teacher1.getExpertise().add(subject1);
                    teacher1.getExpertise().add(subject3);
                    teacherRepository.save(teacher1);

                    teacher2.getExpertise().add(subject2);
                    teacher2.getExpertise().add(subject4);
                    teacherRepository.save(teacher2);

                    System.out.println("✓ Teacher expertise assigned successfully!");

                    // Add subjects to sections - fetch fresh sections within the transaction
                    List<Section> sections = sectionRepository.findAll();
                    if (sections.size() >= 2) {
                        Section section1 = sections.get(0);
                        Section section2 = sections.get(1);

                        section1.getSubjects().add(subject1);
                        section1.getSubjects().add(subject3);
                        sectionRepository.save(section1);

                        section2.getSubjects().add(subject2);
                        section2.getSubjects().add(subject4);
                        sectionRepository.save(section2);

                        System.out.println("✓ Subjects assigned to sections successfully!");
                    }

                    // Create elective subjects (for all sections)
                    Subject electiveSubject1 = new Subject();
                    electiveSubject1.setName("AI & Machine Learning");
                    electiveSubject1.setRoomTypeRequirement(RoomType.CR);
                    electiveSubject1.setDescription("Elective: Advanced AI techniques");
                    electiveSubject1.setLecturesPerWeek(2);
                    electiveSubject1.setElective(true); // Mark as elective
                    electiveSubject1 = subjectRepository.save(electiveSubject1);

                    Subject electiveSubject2 = new Subject();
                    electiveSubject2.setName("Cloud Computing");
                    electiveSubject2.setRoomTypeRequirement(RoomType.LAB);
                    electiveSubject2.setDescription("Elective: Cloud platforms and services");
                    electiveSubject2.setLecturesPerWeek(2);
                    electiveSubject2.setElective(true); // Mark as elective
                    electiveSubject2 = subjectRepository.save(electiveSubject2);

                    System.out.println("✓ 2 Elective Subjects initialized successfully!");

                    // Add elective subjects to teacher expertise
                    teacher1.getExpertise().add(electiveSubject1);
                    teacher2.getExpertise().add(electiveSubject2);
                    teacherRepository.save(teacher1);
                    teacherRepository.save(teacher2);

                    System.out.println("✓ Elective expertise assigned to teachers!");
                }
            }

            if (bookingRepository.count() == 0) {
                // Create and save Bookings
                Room room1 = roomRepository.findAll().get(0);
                Room room2 = roomRepository.findAll().get(1);
                Teacher teacher1 = teacherRepository.findAll().get(0);
                Teacher teacher2 = teacherRepository.findAll().get(1);
                Section section1 = sectionRepository.findAll().get(0);
                Section section2 = sectionRepository.findAll().get(1);

                // Get subjects
                Subject subject1 = subjectRepository.findAll().get(0);
                Subject subject2 = subjectRepository.findAll().get(1);

                Booking booking1 = new Booking();
                booking1.setRoom(room1);
                booking1.setTeacher(teacher1);
                booking1.setSection(section1);
                booking1.setSubject(subject1);
                booking1.setSlotId(1);
                booking1.setBookingDate(LocalDate.now());
                bookingRepository.save(booking1);

                Booking booking2 = new Booking();
                booking2.setRoom(room2);
                booking2.setTeacher(teacher2);
                booking2.setSection(section2);
                booking2.setSubject(subject2);
                booking2.setSlotId(2);
                booking2.setBookingDate(LocalDate.now());
                bookingRepository.save(booking2);

                Booking booking3 = new Booking();
                booking3.setRoom(room1);
                booking3.setTeacher(teacher2);
                booking3.setSection(section1);
                booking3.setSubject(subject1);
                booking3.setSlotId(3);
                booking3.setBookingDate(LocalDate.now().plusDays(1));
                bookingRepository.save(booking3);

                System.out.println("✓ 3 Bookings initialized successfully!");
            }

            // Initialize Elective Groups
            if (electiveGroupRepository.count() == 0) {
                // Get all sections
                List<Section> allSections = sectionRepository.findAll();

                if (allSections.size() >= 3) {
                    Section sectionA1 = allSections.get(0); // A1
                    Section sectionA2 = allSections.get(1); // A2
                    Section sectionB1 = allSections.get(2); // B1

                    // Create Elective Group G1: AI & ML (all sections combined)
                    ElectiveGroup group1 = new ElectiveGroup();
                    group1.setGroupName("G1");
                    group1.setDescription("AI & Machine Learning Elective (A1, A2, B1 combined)");
                    group1.setActive(true);
                    group1.getSections().add(sectionA1);
                    group1.getSections().add(sectionA2);
                    group1.getSections().add(sectionB1);
                    electiveGroupRepository.save(group1);

                    System.out.println("✓ Elective Group G1 created!");
                    System.out.println("  - Total students in G1: " + group1.getTotalStudentCount() + 
                                      " (A1: 45 + A2: 48 + B1: 42)");

                    // Create Elective Group G2: Cloud Computing (different combinations)
                    if (allSections.size() >= 2) {
                        ElectiveGroup group2 = new ElectiveGroup();
                        group2.setGroupName("G2");
                        group2.setDescription("Cloud Computing Elective (A1, A2 combined)");
                        group2.setActive(true);
                        group2.getSections().add(sectionA1);
                        group2.getSections().add(sectionA2);
                        electiveGroupRepository.save(group2);

                        System.out.println("✓ Elective Group G2 created!");
                        System.out.println("  - Total students in G2: " + group2.getTotalStudentCount() + 
                                          " (A1: 45 + A2: 48)");
                    }
                }

                System.out.println("✓ Elective Groups initialized successfully!");
            }

            System.out.println("✓ Database initialization completed!");
        };
    }
}
