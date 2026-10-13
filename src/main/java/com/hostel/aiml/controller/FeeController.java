
package com.hostel.aiml.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.aiml.entity.Fee;
import com.hostel.aiml.entity.Student;
import com.hostel.aiml.repository.FeeRepository;
import com.hostel.aiml.repository.StudentRepository;

@RestController
@RequestMapping("/api/student/fees")
public class FeeController {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;

    public FeeController(FeeRepository feeRepository,
                         StudentRepository studentRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public ResponseEntity<?> getMyFees(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first.");
        }

        Optional<Student> studentOptional =
                studentRepository.findByEmail(authentication.getName());

        if (studentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student account not found.");
        }

        List<Fee> fees = feeRepository
                .findByStudentIdOrderByDueDateAsc(studentOptional.get().getId());

        return ResponseEntity.ok(fees);
    }
}
