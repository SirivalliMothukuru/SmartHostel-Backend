
package com.hostel.aiml.controller;

import com.hostel.aiml.entity.LeaveRequest;
import com.hostel.aiml.repository.LeaveRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/warden/gate-passes")
public class WardenGatePassController {

    private final LeaveRequestRepository leaveRequestRepository;

    public WardenGatePassController(
            LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<LeaveRequest> requests =
                leaveRequestRepository.findByStatusOrderByCreatedAtDesc("PENDING");

        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return leaveRequestRepository.findById(id)
                .map(request -> {
                    if (!"PENDING".equals(request.getStatus())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message",
                                        "Only pending requests can be approved"));
                    }

                    request.setStatus("APPROVED");
                    request.setApprovedAt(LocalDateTime.now());
                    request.setQrToken(UUID.randomUUID().toString());

                    LeaveRequest saved = leaveRequestRepository.save(request);

                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Gate pass request not found")));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String rejectionReason = body.getOrDefault("rejectionReason", "").trim();

        if (rejectionReason.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Rejection reason is required"));
        }

        return leaveRequestRepository.findById(id)
                .map(request -> {
                    if (!"PENDING".equals(request.getStatus())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message",
                                        "Only pending requests can be rejected"));
                    }

                    request.setStatus("REJECTED");
                    request.setRejectionReason(rejectionReason);

                    LeaveRequest saved = leaveRequestRepository.save(request);

                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Gate pass request not found")));
    }
}
