package com.timetable.backend.controller;

import com.timetable.backend.dto.LoginRequest;
import com.timetable.backend.dto.LoginResponse;
import com.timetable.backend.model.Teacher;
import com.timetable.backend.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String iden = loginRequest.getIdentifier();
        Optional<Teacher> teacherOpt = teacherService.findByEmail(iden);
        if (teacherOpt.isEmpty()) {
            teacherOpt = teacherService.findByUsername(iden);
        }
        
        if (teacherOpt.isPresent()) {
            Teacher teacher = teacherOpt.get();
            
            // Check if account is approved
            if (!teacher.isApproved()) {
                return ResponseEntity.status(403).body(new LoginResponse(
                    false, 
                    "Account pending approval from Admin/HOD", 
                    null, null, null, null, null
                ));
            }

            // Simple password check (In production, use BCrypt)
            if (teacher.getPassword() != null && teacher.getPassword().equals(loginRequest.getPassword())) {
                return ResponseEntity.ok(new LoginResponse(
                    true, 
                    "Login successful", 
                    "mock-jwt-token", 
                    teacher.getId(), 
                    teacher.getName(), 
                    teacher.getEmail(),
                    teacher.getRole()
                ));
            }
        }
        
        return ResponseEntity.status(401).body(new LoginResponse(false, "Invalid username/email or password", null, null, null, null, null));
    }
}
