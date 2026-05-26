package com.timetable.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for a single teaching assignment slot.
 * sectionIds contains one or more section IDs that form a combined lecture group.
 * For example, sectionIds=[1,2] means section A1 and A2 study together in one slot.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeachingAssignmentDTO {
    /** List of section IDs forming this grouped slot (one or more). */
    private List<Long> sectionIds;

    /** Subject ID for this slot. */
    private Long subjectId;
}
