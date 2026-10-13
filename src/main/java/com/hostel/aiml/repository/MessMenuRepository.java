
package com.hostel.aiml.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hostel.aiml.entity.MessMenu;

public interface MessMenuRepository extends JpaRepository<MessMenu, Long> {

    List<MessMenu> findByDayIgnoreCase(String day);

    List<MessMenu> findByMealTypeIgnoreCase(String mealType);
}
