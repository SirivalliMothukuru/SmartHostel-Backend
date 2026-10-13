
package com.hostel.aiml.controller;

import com.hostel.aiml.entity.Student;
import com.hostel.aiml.entity.User;
import com.hostel.aiml.repository.StudentRepository;
import com.hostel.aiml.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warden/students")
@PreAuthorize("hasRole('WARDEN')")
public class WardenStudentController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public WardenStudentController(
            StudentRepository studentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private User getWarden(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName())
                .filter(user -> "WARDEN".equalsIgnoreCase(user.getRole()))
                .orElse(null);
    }

    private Map<String, Object> studentResponse(Student student) {
        return Map.of(
                "id", student.getId(),
                "name", student.getName() == null ? "" : student.getName(),
                "email", student.getEmail() == null ? "" : student.getEmail(),
                "phone", student.getPhone() == null ? "" : student.getPhone(),
                "rollNumber", student.getRollNumber() == null
                        ? "" : student.getRollNumber()
        );
    }

    @GetMapping
    public ResponseEntity<?> getStudents(Authentication authentication) {
        if (getWarden(authentication) == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Warden access required"));
        }

        List<Map<String, Object>> students = studentRepository.findAll()
                .stream()
                .map(this::studentResponse)
                .toList();

        return ResponseEntity.ok(students);
    }

    @PostMapping
    public ResponseEntity<?> addStudent(
            @RequestBody Map<String, String> data,
            Authentication authentication) {

        if (getWarden(authentication) == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Warden access required"));
        }

        String name = data.get("name");
        String email = data.get("email");
        String password = data.get("password");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Name, email and password are required"));
        }

        email = email.trim();

        if (studentRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already exists"));
        }

        Student student = new Student();
        student.setName(name.trim());
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(password));
        student.setPhone(data.getOrDefault("phone", ""));
        student.setRollNumber(data.getOrDefault("rollNumber", ""));

        Student saved = studentRepository.save(student);

        return ResponseEntity.status(201).body(studentResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            Authentication authentication) {

        if (getWarden(authentication) == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Warden access required"));
        }

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Student not found"));
        }

        String name = data.get("name");
        String email = data.get("email");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Name and email are required"));
        }

        email = email.trim();

        boolean emailUsed = studentRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();

        if (emailUsed) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already exists"));
        }

        student.setName(name.trim());
        student.setEmail(email);
        student.setPhone(data.getOrDefault("phone", ""));
        student.setRollNumber(data.getOrDefault("rollNumber", ""));

        String password = data.get("password");

        if (password != null && !password.isBlank()) {
            student.setPassword(passwordEncoder.encode(password));
        }

        Student updated = studentRepository.save(student);

        return ResponseEntity.ok(studentResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable Long id,
            Authentication authentication) {

        if (getWarden(authentication) == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Warden access required"));
        }

        Student student = studentRepository.findById(id).orElse(null);

        if (student == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Student not found"));
        }

        studentRepository.delete(student);

        return ResponseEntity.ok(
                Map.of("message", "Student deleted successfully"));
    }
}
