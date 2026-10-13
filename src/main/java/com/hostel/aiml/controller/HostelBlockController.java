package com.hostel.aiml.controller;

import com.hostel.aiml.entity.HostelBlock;
import com.hostel.aiml.repository.HostelBlockRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
@CrossOrigin("*")
public class HostelBlockController {

    private final HostelBlockRepository repository;

    public HostelBlockController(HostelBlockRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public HostelBlock createBlock(@RequestBody HostelBlock block) {
        return repository.save(block);
    }

    @GetMapping
    public List<HostelBlock> getAllBlocks() {
        return repository.findAll();
    }
}