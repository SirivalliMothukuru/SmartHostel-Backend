package com.hostel.aiml.controller;

import com.hostel.aiml.entity.Student;
import com.hostel.aiml.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin("*")
public class StudentController {

    private final StudentRepository repository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public Student registerStudent(@RequestBody Student student) {

        student.setPassword(passwordEncoder.encode(student.getPassword()));

        return repository.save(student);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return repository.findAll();
    }
}