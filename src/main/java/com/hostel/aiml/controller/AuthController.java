
package com.hostel.aiml.controller;

import com.hostel.aiml.entity.User;
import com.hostel.aiml.jwt.JwtService;
import com.hostel.aiml.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Set<String> ALLOWED_ROLES =
            Set.of("STUDENT", "WARDEN", "ADMIN", "SECURITY");

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> request) {

        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");
        String role = request.get("role");
        String block = request.get("block");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Name, email, and password of at least 8 characters are required"));
        }

        email = email.trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already registered"));
        }

        if (role == null || role.isBlank()) {
            role = "STUDENT";
        } else {
            role = role.trim().toUpperCase();
        }

        if (!ALLOWED_ROLES.contains(role)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid role selected"));
        }

        // Public registration is limited to students.
        // Staff and admin accounts must be provisioned securely.
        if (!role.equals("STUDENT")) {
            return ResponseEntity.status(403)
                    .body(Map.of("message",
                            "Warden, Admin, and Security accounts must be created by an authorized administrator"));
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT");
        user.setBlock("BOYS");

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "email", savedUser.getEmail(),
                "role", savedUser.getRole(),
                "block", savedUser.getBlock()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> loginRequest) {

        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        String selectedRole = loginRequest.get("role");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()
                || selectedRole == null || selectedRole.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Email, password, and role are required"));
        }

        selectedRole = selectedRole.trim().toUpperCase();

        if (!ALLOWED_ROLES.contains(selectedRole)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid role selected"));
        }

        User user = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (user == null
                || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid email or password"));
        }

        String actualRole = user.getRole() == null
                ? ""
                : user.getRole().trim().toUpperCase();

        if (!actualRole.equals(selectedRole)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message",
                            "The selected role does not match this account"));
        }

        String token = jwtService.generateToken(user.getEmail());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", user.getEmail(),
                "role", actualRole,
                "block", user.getBlock() == null ? "" : user.getBlock()
        ));
    }
}
