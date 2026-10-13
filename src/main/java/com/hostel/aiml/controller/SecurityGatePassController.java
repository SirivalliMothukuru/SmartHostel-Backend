
package com.hostel.aiml.controller;

import com.hostel.aiml.entity.LeaveRequest;
import com.hostel.aiml.entity.User;
import com.hostel.aiml.repository.LeaveRequestRepository;
import com.hostel.aiml.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/security/gate-passes")
public class SecurityGatePassController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public SecurityGatePassController(
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    private User getSecurityStaff(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName())
                .filter(user -> "SECURITY".equalsIgnoreCase(user.getRole()))
                .orElse(null);
    }

    private boolean isWithinDateRange(LeaveRequest pass) {
        LocalDate today = LocalDate.now();

        return pass.getFromDate() != null
                && pass.getToDate() != null
                && !today.isBefore(pass.getFromDate())
                && !today.isAfter(pass.getToDate());
    }

    private boolean hasBlockAccess(User staff, LeaveRequest pass) {
        return staff.getBlock() != null
                && pass.getHostelBlock() != null
                && staff.getBlock().equalsIgnoreCase(pass.getHostelBlock());
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifyPass(
            @PathVariable String token,
            Authentication authentication) {

        User staff = getSecurityStaff(authentication);

        if (staff == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Security staff access required"));
        }

        return leaveRequestRepository.findByQrToken(token)
                .map(pass -> {
                    if (!hasBlockAccess(staff, pass)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message",
                                        "You cannot verify passes for another hostel block"));
                    }

                    if (!"APPROVED".equals(pass.getStatus())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "This gate pass is not approved"));
                    }

                    if (!isWithinDateRange(pass)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message",
                                        "Gate pass is outside its valid date range"));
                    }

                    return ResponseEntity.ok(Map.of(
                            "message", "Gate pass is valid",
                            "passId", pass.getId(),
                            "studentId", pass.getStudentId(),
                            "hostelBlock", pass.getHostelBlock(),
                            "fromDate", pass.getFromDate().toString(),
                            "toDate", pass.getToDate().toString(),
                            "status", pass.getStatus(),
                            "exitRecorded", pass.getExitAt() != null,
                            "returnRecorded", pass.getReturnAt() != null
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Gate pass not found")));
    }

    @PostMapping("/{id}/exit")
    public ResponseEntity<?> recordExit(
            @PathVariable Long id,
            Authentication authentication) {

        User staff = getSecurityStaff(authentication);

        if (staff == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Security staff access required"));
        }

        return leaveRequestRepository.findById(id)
                .map(pass -> {
                    if (!hasBlockAccess(staff, pass)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message",
                                        "You cannot manage another hostel block"));
                    }

                    if (!"APPROVED".equals(pass.getStatus())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Only approved passes can be used"));
                    }

                    if (!isWithinDateRange(pass)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message",
                                        "Gate pass is outside its valid date range"));
                    }

                    if (pass.getExitAt() != null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Exit has already been recorded"));
                    }

                    pass.setExitAt(LocalDateTime.now());
                    LeaveRequest saved = leaveRequestRepository.save(pass);

                    return ResponseEntity.ok(Map.of(
                            "message", "Student exit recorded",
                            "passId", saved.getId(),
                            "exitAt", saved.getExitAt().toString()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Gate pass not found")));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> recordReturn(
            @PathVariable Long id,
            Authentication authentication) {

        User staff = getSecurityStaff(authentication);

        if (staff == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Security staff access required"));
        }

        return leaveRequestRepository.findById(id)
                .map(pass -> {
                    if (!hasBlockAccess(staff, pass)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message",
                                        "You cannot manage another hostel block"));
                    }

                    if (!"APPROVED".equals(pass.getStatus())) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Only approved passes can be used"));
                    }

                    if (!isWithinDateRange(pass)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message",
                                        "Gate pass is outside its valid date range"));
                    }

                    if (pass.getExitAt() == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message",
                                        "Record the exit before recording return"));
                    }

                    if (pass.getReturnAt() != null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Return has already been recorded"));
                    }

                    pass.setReturnAt(LocalDateTime.now());
                    LeaveRequest saved = leaveRequestRepository.save(pass);

                    return ResponseEntity.ok(Map.of(
                            "message", "Student return recorded",
                            "passId", saved.getId(),
                            "returnAt", saved.getReturnAt().toString()
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Gate pass not found")));
    }
}
