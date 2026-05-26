package com.timetable.backend.controller;
import org.springframework.http.ResponseEntity;

import com.timetable.backend.model.Section;
import com.timetable.backend.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sections")
@CrossOrigin("*")
public class SectionController {
    @Autowired
    private SectionService sectionService;

    @GetMapping("/all")
    public List<Section> getAllSections() {
        return sectionService.getAllSections();
    }

    @PostMapping
    public Section createSection(@RequestBody Section section) {
        return sectionService.saveSection(section);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Section> updateSection(@PathVariable Long id, @RequestBody Section sectionDetails) {
        return sectionService.getSectionById(id)
                .map(section -> {
                    section.setName(sectionDetails.getName());
                    section.setBatchYear(sectionDetails.getBatchYear());
                    section.setStudentCount(sectionDetails.getStudentCount());
                    return ResponseEntity.ok(sectionService.saveSection(section));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok().build();
    }
}
