package com.timetable.backend.repository;

import com.timetable.backend.model.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    List<TeachingAssignment> findByTeacherId(Long teacherId);

    List<TeachingAssignment> findBySubjectId(Long subjectId);

    /**
     * Check if an assignment already exists for teacher + subject
     * where any of the given sections are already covered.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM TeachingAssignment a " +
           "JOIN a.sections s " +
           "WHERE a.teacher.id = :teacherId AND s.id = :sectionId AND a.subject.id = :subjectId")
    boolean existsByTeacherIdAndSectionIdAndSubjectId(
            @Param("teacherId") Long teacherId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId);

    /**
     * Find all assignments that contain a specific section.
     */
    @Query("SELECT a FROM TeachingAssignment a JOIN a.sections s WHERE s.id = :sectionId")
    List<TeachingAssignment> findBySectionId(@Param("sectionId") Long sectionId);

    @Transactional
    void deleteByTeacherId(Long teacherId);

    /**
     * Delete assignments that contain a given section by finding and deleting them.
     * Since @ManyToMany we cannot use Spring's derived deleteBySectionId.
     */
    @Query("SELECT a FROM TeachingAssignment a JOIN a.sections s WHERE s.id = :sectionId")
    List<TeachingAssignment> findAssignmentsContainingSection(@Param("sectionId") Long sectionId);
}
