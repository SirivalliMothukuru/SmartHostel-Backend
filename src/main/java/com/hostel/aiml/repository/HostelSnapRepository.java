
package com.hostel.aiml.repository;

import com.hostel.aiml.entity.HostelSnap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HostelSnapRepository extends JpaRepository<HostelSnap, Long> {

    List<HostelSnap> findByHostelBlockOrderByCreatedAtDesc(String hostelBlock);

    List<HostelSnap> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
