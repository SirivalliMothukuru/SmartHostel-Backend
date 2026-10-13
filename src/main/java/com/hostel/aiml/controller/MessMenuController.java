
package com.hostel.aiml.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.aiml.entity.MessMenu;
import com.hostel.aiml.repository.MessMenuRepository;

@RestController
@RequestMapping("/api/student/mess-menu")
public class MessMenuController {

    private final MessMenuRepository messMenuRepository;

    public MessMenuController(MessMenuRepository messMenuRepository) {
        this.messMenuRepository = messMenuRepository;
    }

    @GetMapping
    public ResponseEntity<?> getMessMenu(
            Authentication authentication,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String mealType) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first.");
        }

        List<MessMenu> menu;

        if (day != null && !day.isBlank()) {
            menu = messMenuRepository.findByDayIgnoreCase(day.trim());
        } else if (mealType != null && !mealType.isBlank()) {
            menu = messMenuRepository.findByMealTypeIgnoreCase(mealType.trim());
        } else {
            menu = messMenuRepository.findAll();
        }

        return ResponseEntity.ok(menu);
    }
}
