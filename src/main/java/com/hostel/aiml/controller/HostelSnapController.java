
package com.hostel.aiml.controller;

import com.hostel.aiml.entity.HostelSnap;
import com.hostel.aiml.repository.HostelSnapRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snaps")
@CrossOrigin(origins = "http://localhost:5173")
public class HostelSnapController {

    private final HostelSnapRepository repository;

    public HostelSnapController(HostelSnapRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> createSnap(@RequestBody HostelSnap snap) {
        if (snap.getStudentId() == null ||
            snap.getTitle() == null || snap.getTitle().isBlank() ||
            snap.getCategory() == null || snap.getCategory().isBlank() ||
            snap.getLocation() == null || snap.getLocation().isBlank() ||
            snap.getDescription() == null || snap.getDescription().isBlank() ||
            snap.getHostelBlock() == null || snap.getHostelBlock().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Required fields are missing"));
        }

        snap.setId(null);
        snap.setStatus("PENDING");

        HostelSnap saved = repository.save(snap);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<HostelSnap> getSnaps(@RequestParam String block) {
        return repository.findByHostelBlockOrderByCreatedAtDesc(block);
    }

    @GetMapping("/my")
    public List<HostelSnap> getMySnaps(@RequestParam Long studentId) {
        return repository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateSnapStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        HostelSnap snap = repository.findById(id).orElse(null);

        if (snap == null) {
            return ResponseEntity.notFound().build();
        }

        String status = body.get("status");

        if (status == null ||
            !List.of("PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED")
                    .contains(status)) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid status"));
        }

        snap.setStatus(status);

        return ResponseEntity.ok(repository.save(snap));
    }
}
