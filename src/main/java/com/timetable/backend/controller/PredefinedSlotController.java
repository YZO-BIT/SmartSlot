package com.timetable.backend.controller;

import com.timetable.backend.model.PredefinedSlot;
import com.timetable.backend.repository.PredefinedSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/predefinedSlots")
@CrossOrigin(origins = "*")
public class PredefinedSlotController {

    @Autowired
    private PredefinedSlotRepository predefinedSlotRepository;

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<PredefinedSlot>> getSlotsByTeacher(@PathVariable Long teacherId) {
        List<PredefinedSlot> slots = predefinedSlotRepository.findAll()
                .stream()
                .filter(slot -> slot.getTeacher() != null && slot.getTeacher().getId().equals(teacherId))
                .toList();
        return ResponseEntity.ok(slots);
    }
}
