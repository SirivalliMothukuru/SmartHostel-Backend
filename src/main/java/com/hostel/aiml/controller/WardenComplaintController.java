
package com.hostel.aiml.controller;

import com.hostel.aiml.entity.Complaint;
import com.hostel.aiml.entity.User;
import com.hostel.aiml.repository.ComplaintRepository;
import com.hostel.aiml.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/warden/complaints")
@PreAuthorize("hasRole('WARDEN')")
public class WardenComplaintController {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public WardenComplaintController(
            ComplaintRepository complaintRepository,
            UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    private String getWardenBlock(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        Optional<User> user =
                userRepository.findByEmail(authentication.getName());

        if (user.isEmpty() || user.get().getRole() == null
                || !user.get().getRole().toString().equalsIgnoreCase("WARDEN")) {
            return null;
        }

        return user.get().getBlock();
    }

    @GetMapping
    public ResponseEntity<?> getComplaints(Authentication authentication) {
        String block = getWardenBlock(authentication);

        if (block == null || block.isBlank()) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Warden block not configured."));
        }

        List<Complaint> complaints =
                complaintRepository.findByHostelBlockOrderByCreatedAtDesc(block);

        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String block = getWardenBlock(authentication);

        if (block == null || block.isBlank()) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Warden block not configured."));
        }

        Optional<Complaint> result = complaintRepository.findById(id);

        if (result.isEmpty()
                || !block.equalsIgnoreCase(result.get().getHostelBlock())) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Complaint not found in your block."));
        }

        String status = body.get("status");

        if (status == null || !List.of(
                "PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"
        ).contains(status.toUpperCase())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid complaint status."));
        }

        Complaint complaint = result.get();
        complaint.setStatus(status.toUpperCase());

        return ResponseEntity.ok(complaintRepository.save(complaint));
    }
}
