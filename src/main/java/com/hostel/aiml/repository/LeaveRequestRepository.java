
package com.hostel.aiml.repository;

import com.hostel.aiml.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<LeaveRequest> findByHostelBlockOrderByCreatedAtDesc(String hostelBlock);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status);

    List<LeaveRequest> findByHostelBlockAndStatusOrderByCreatedAtDesc(
            String hostelBlock, String status);

    Optional<LeaveRequest> findByQrToken(String qrToken);
}
