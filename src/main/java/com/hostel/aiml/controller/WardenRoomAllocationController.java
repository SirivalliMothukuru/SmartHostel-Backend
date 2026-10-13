
package com.hostel.aiml.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hostel.aiml.entity.RoomAllocation;
import com.hostel.aiml.repository.RoomAllocationRepository;

@RestController
@RequestMapping("/api/warden/room-allocations")
@PreAuthorize("hasRole('WARDEN')")
public class WardenRoomAllocationController {

    private final RoomAllocationRepository repository;

    public WardenRoomAllocationController(RoomAllocationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<RoomAllocation> getAllocations() {
        return repository.findAll();
    }

    @GetMapping("/block/{block}")
    public List<RoomAllocation> getByBlock(@PathVariable String block) {
        return repository.findByHostelBlockOrderByFloorAscRoomNumberAsc(
                block.toUpperCase());
    }

    @PostMapping
    public RoomAllocation assignRoom(@RequestBody RoomAllocation allocation) {
        allocation.setHostelBlock(allocation.getHostelBlock().toUpperCase());

        if (repository.existsByStudentId(allocation.getStudentId())) {
            throw new IllegalArgumentException(
                    "This student already has a room allocation.");
        }

        if (repository.existsByHostelBlockAndRoomNumberAndBedNumber(
                allocation.getHostelBlock(),
                allocation.getRoomNumber(),
                allocation.getBedNumber())) {
            throw new IllegalArgumentException(
                    "This bed is already allocated.");
        }

        allocation.setStatus("ALLOCATED");
        return repository.save(allocation);
    }

    @PutMapping("/{id}")
    public RoomAllocation updateAllocation(
            @PathVariable Long id,
            @RequestBody RoomAllocation updated) {

        RoomAllocation existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room allocation not found."));

        if (updated.getStudentId() == null ||
                updated.getHostelBlock() == null ||
                updated.getRoomNumber() == null ||
                updated.getFloor() == null ||
                updated.getBedNumber() == null ||
                updated.getRoomType() == null) {
            throw new IllegalArgumentException(
                    "All allocation fields are required.");
        }

        String block = updated.getHostelBlock().toUpperCase();

        if (repository.findByStudentId(updated.getStudentId())
                .filter(a -> !a.getId().equals(id)).isPresent()) {
            throw new IllegalArgumentException(
                    "This student already has another allocation.");
        }

        if (repository.existsByHostelBlockAndRoomNumberAndBedNumber(
                block, updated.getRoomNumber(), updated.getBedNumber())
                && !existing.getHostelBlock().equalsIgnoreCase(block)) {
            throw new IllegalArgumentException(
                    "This bed is already allocated.");
        }

        existing.setStudentId(updated.getStudentId());
        existing.setHostelBlock(block);
        existing.setRoomNumber(updated.getRoomNumber());
        existing.setFloor(updated.getFloor());
        existing.setBedNumber(updated.getBedNumber());
        existing.setRoomType(updated.getRoomType());

        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteAllocation(@PathVariable Long id) {
        RoomAllocation allocation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Room allocation not found."));

        repository.delete(allocation);
        return Map.of("message", "Room allocation removed successfully.");
    }
}
