
package com.hostel.aiml.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.aiml.entity.Student;
import com.hostel.aiml.repository.StudentRepository;

@RestController
@RequestMapping("/api/student")
public class StudentProfileController {

    private final StudentRepository studentRepository;

    public StudentProfileController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/test")
    public String test() {
        return "Student controller is working";
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();

        return studentRepository.findByEmail(email)
                .<ResponseEntity<?>>map(student -> {
                    Map<String, Object> profile = new LinkedHashMap<>();

                    profile.put("id", student.getId());
                    profile.put("name", student.getName());
                    profile.put("email", student.getEmail());
                    profile.put("phone", student.getPhone());
                    profile.put("rollNumber", student.getRollNumber());

                    return ResponseEntity.ok(profile);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();

        return studentRepository.findByEmail(email)
                .<ResponseEntity<?>>map(student -> {

                    if (request.containsKey("name")) {
                        student.setName(request.get("name"));
                    }

                    if (request.containsKey("phone")) {
                        student.setPhone(request.get("phone"));
                    }

                    if (request.containsKey("rollNumber")) {
                        student.setRollNumber(request.get("rollNumber"));
                    }

                    Student saved = studentRepository.save(student);

                    Map<String, Object> profile = new LinkedHashMap<>();

                    profile.put("id", saved.getId());
                    profile.put("name", saved.getName());
                    profile.put("email", saved.getEmail());
                    profile.put("phone", saved.getPhone());
                    profile.put("rollNumber", saved.getRollNumber());

                    return ResponseEntity.ok(profile);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
