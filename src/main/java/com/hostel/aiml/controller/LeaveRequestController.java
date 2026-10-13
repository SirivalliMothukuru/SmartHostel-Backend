
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

import com.hostel.aiml.dto.LeaveRequestRequest;
import com.hostel.aiml.entity.LeaveRequest;
import com.hostel.aiml.entity.User;
import com.hostel.aiml.repository.LeaveRequestRepository;
import com.hostel.aiml.repository.StudentRepository;
import com.hostel.aiml.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/student/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public LeaveRequestController(
            LeaveRequestRepository leaveRequestRepository,
            StudentRepository studentRepository,
            UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createLeaveRequest(
            Authentication authentication,
            @Valid @RequestBody LeaveRequestRequest request) {

        System.out.println("CREATE LEAVE REQUEST REACHED");

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();
        System.out.println("Authenticated email: " + email);

        if (request.getFromDate() == null || request.getToDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Start date and end date are required"));
        }

        if (request.getToDate().isBefore(request.getFromDate())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "End date cannot be before start date"));
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Authenticated account not found"));
        }

        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Student access required"));
        }

        String block = user.getBlock();

        if (block == null ||
                (!"BOYS".equalsIgnoreCase(block)
                && !"GIRLS".equalsIgnoreCase(block))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Hostel block is not configured correctly"));
        }

        var student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message",
                            "Student profile not found for this login email"));
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStudentId(student.getId());
        leaveRequest.setReason(request.getReason().trim());
        leaveRequest.setDescription(request.getDescription().trim());
        leaveRequest.setFromDate(request.getFromDate());
        leaveRequest.setToDate(request.getToDate());
        leaveRequest.setStatus("PENDING");
        leaveRequest.setHostelBlock(block.toUpperCase());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<?> getMyLeaveRequests(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Student access required"));
        }

        var student = studentRepository.findByEmail(email).orElse(null);

        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message",
                            "Student profile not found for this login email"));
        }

        List<LeaveRequest> requests =
                leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(
                        student.getId());

        return ResponseEntity.ok(requests);
    }
}
