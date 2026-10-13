
package com.hostel.aiml.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.aiml.dto.ComplaintRequest;
import com.hostel.aiml.entity.Complaint;
import com.hostel.aiml.entity.Student;
import com.hostel.aiml.repository.ComplaintRepository;
import com.hostel.aiml.repository.StudentRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/student/complaints")
public class ComplaintController {

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;

    public ComplaintController(
            ComplaintRepository complaintRepository,
            StudentRepository studentRepository) {
        this.complaintRepository = complaintRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping
    public ResponseEntity<?> createComplaint(
            Authentication authentication,
            @Valid @RequestBody ComplaintRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return studentRepository.findByEmail(authentication.getName())
                .<ResponseEntity<?>>map(student -> {

                    Complaint complaint = new Complaint();
                    complaint.setStudentId(student.getId());
                    complaint.setTitle(request.getTitle().trim());
                    complaint.setCategory(request.getCategory().trim());
                    complaint.setDescription(request.getDescription().trim());

                    String priority = request.getPriority().trim().toUpperCase();

                    if (!List.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message",
                                        "Priority must be LOW, MEDIUM, or HIGH"));
                    }

                    complaint.setPriority(priority);
                    complaint.setHostelBlock(getStudentBlock(student));

                    Complaint saved = complaintRepository.save(complaint);

                    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found")));
    }

    @GetMapping
    public ResponseEntity<?> getMyComplaints(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return studentRepository.findByEmail(authentication.getName())
                .<ResponseEntity<?>>map(student -> ResponseEntity.ok(
                        complaintRepository.findByStudentIdOrderByCreatedAtDesc(
                                student.getId())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Student not found")));
    }

    private String getStudentBlock(Student student) {
        return "BOYS";
    }
}
