
package com.hostel.aiml.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hostel.aiml.entity.RoomAllocation;

public interface RoomAllocationRepository
        extends JpaRepository<RoomAllocation, Long> {

    Optional<RoomAllocation> findByStudentId(Long studentId);

    List<RoomAllocation> findByHostelBlockOrderByFloorAscRoomNumberAsc(String hostelBlock);

    boolean existsByStudentId(Long studentId);

    boolean existsByHostelBlockAndRoomNumberAndBedNumber(
            String hostelBlock, String roomNumber, String bedNumber);
}
