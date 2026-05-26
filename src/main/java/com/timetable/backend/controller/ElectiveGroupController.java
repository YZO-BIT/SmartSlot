package com.timetable.backend.controller;

import com.timetable.backend.model.ElectiveGroup;
import com.timetable.backend.repository.ElectiveGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/elective-groups")
public class ElectiveGroupController {

    @Autowired
    private ElectiveGroupRepository electiveGroupRepository;

    /**
     * Create a new elective group
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createElectiveGroup(@RequestBody ElectiveGroup group) {
        ElectiveGroup savedGroup = electiveGroupRepository.save(group);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Created");
        response.put("groupId", savedGroup.getId());
        response.put("groupName", savedGroup.getGroupName());
        response.put("sectionsCount", savedGroup.getSections().size());
        response.put("totalStudents", savedGroup.getTotalStudentCount());
        response.put("message", "Elective group created successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all elective groups
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllElectiveGroups() {
        List<ElectiveGroup> groups = electiveGroupRepository.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Success");
        response.put("totalGroups", groups.size());
        response.put("groups", groups.stream().map(g -> {
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("id", g.getId());
            groupInfo.put("groupName", g.getGroupName());
            groupInfo.put("description", g.getDescription());
            groupInfo.put("sectionsCount", g.getSections().size());
            groupInfo.put("totalStudents", g.getTotalStudentCount());
            groupInfo.put("isActive", g.isActive());
            return groupInfo;
        }).toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get elective group by ID
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> getElectiveGroupById(@PathVariable Long groupId) {
        Optional<ElectiveGroup> groupOpt = electiveGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "Not Found");
            error.put("message", "Elective group not found");
            return ResponseEntity.notFound().build();
        }

        ElectiveGroup group = groupOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", group.getId());
        response.put("groupName", group.getGroupName());
        response.put("description", group.getDescription());
        response.put("sections", group.getSections().stream().map(s -> {
            Map<String, Object> sectionInfo = new HashMap<>();
            sectionInfo.put("id", s.getId());
            sectionInfo.put("name", s.getName());
            sectionInfo.put("studentCount", s.getStudentCount());
            return sectionInfo;
        }).toList());
        response.put("totalStudents", group.getTotalStudentCount());
        response.put("isActive", group.isActive());

        return ResponseEntity.ok(response);
    }

    /**
     * Update elective group
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> updateElectiveGroup(
            @PathVariable Long groupId,
            @RequestBody ElectiveGroup updatedGroup) {

        Optional<ElectiveGroup> groupOpt = electiveGroupRepository.findById(groupId);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ElectiveGroup group = groupOpt.get();
        group.setGroupName(updatedGroup.getGroupName());
        group.setDescription(updatedGroup.getDescription());
        group.setActive(updatedGroup.isActive());
        group.setSections(updatedGroup.getSections());

        ElectiveGroup savedGroup = electiveGroupRepository.save(group);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "Updated");
        response.put("groupId", savedGroup.getId());
        response.put("groupName", savedGroup.getGroupName());
        response.put("totalStudents", savedGroup.getTotalStudentCount());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete elective group
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, String>> deleteElectiveGroup(@PathVariable Long groupId) {
        if (!electiveGroupRepository.existsById(groupId)) {
            return ResponseEntity.notFound().build();
        }

        electiveGroupRepository.deleteById(groupId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "Deleted");
        response.put("message", "Elective group deleted successfully");

        return ResponseEntity.ok(response);
    }
}
