
package com.hostel.aiml.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hostel.aiml.entity.Fee;

public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByStudentIdOrderByDueDateAsc(Long studentId);
}
