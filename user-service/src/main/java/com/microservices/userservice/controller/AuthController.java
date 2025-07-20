package com.microservices.userservice.controller;

import com.microservices.userservice.model.AuthResponse;
import com.microservices.userservice.model.LoginRequest;
import com.microservices.userservice.model.User;
import com.microservices.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            
            User responseUser = new User();
            responseUser.setId(registeredUser.getId());
            responseUser.setUsername(registeredUser.getUsername());
            responseUser.setEmail(registeredUser.getEmail());
            responseUser.setFirstName(registeredUser.getFirstName());
            responseUser.setLastName(registeredUser.getLastName());
            responseUser.setRoles(registeredUser.getRoles());
            responseUser.setCreatedAt(registeredUser.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User registered successfully",
                "user", responseUser
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = userService.authenticateUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Token is required"));
            }

            boolean isValid = userService.validateToken(token);
            if (isValid) {
                User user = userService.getUserFromToken(token);
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", user.getUsername(),
                    "userId", user.getId(),
                    "roles", user.getRoles()
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            if (!userService.validateToken(token)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
            }

            User user = userService.getUserFromToken(token);
            
            User responseUser = new User();
            responseUser.setId(user.getId());
            responseUser.setUsername(user.getUsername());
            responseUser.setEmail(user.getEmail());
            responseUser.setFirstName(user.getFirstName());
            responseUser.setLastName(user.getLastName());
            responseUser.setRoles(user.getRoles());
            responseUser.setCreatedAt(user.getCreatedAt());
            responseUser.setLastLogin(user.getLastLogin());

            return ResponseEntity.ok(responseUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}