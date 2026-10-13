
package com.hostel.aiml.repository;

import com.hostel.aiml.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Complaint> findByHostelBlockOrderByCreatedAtDesc(String hostelBlock);

    List<Complaint> findByStatusOrderByCreatedAtDesc(String status);

    List<Complaint> findByStudentIdAndStatusOrderByCreatedAtDesc(
            Long studentId, String status);
}
