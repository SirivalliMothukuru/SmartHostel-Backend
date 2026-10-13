
package com.hostel.aiml.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.aiml.entity.RoomAllocation;
import com.hostel.aiml.entity.Student;
import com.hostel.aiml.repository.RoomAllocationRepository;
import com.hostel.aiml.repository.StudentRepository;

@RestController
@RequestMapping("/api/student/room")
public class RoomAllocationController {

    private final RoomAllocationRepository roomAllocationRepository;
    private final StudentRepository studentRepository;

    public RoomAllocationController(
            RoomAllocationRepository roomAllocationRepository,
            StudentRepository studentRepository) {
        this.roomAllocationRepository = roomAllocationRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public ResponseEntity<?> getMyRoom(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in to view your room.");
        }

        Optional<Student> studentOptional =
                studentRepository.findByEmail(authentication.getName());

        if (studentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Student account not found.");
        }

        Optional<RoomAllocation> roomOptional =
                roomAllocationRepository.findByStudentId(
                        studentOptional.get().getId());

        if (roomOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No room has been allocated to you yet.");
        }

        return ResponseEntity.ok(roomOptional.get());
    }
}
