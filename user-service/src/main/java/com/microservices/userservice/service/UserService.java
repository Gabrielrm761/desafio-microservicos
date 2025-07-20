package com.microservices.userservice.service;

import com.microservices.userservice.model.AuthResponse;
import com.microservices.userservice.model.LoginRequest;
import com.microservices.userservice.model.User;
import com.microservices.userservice.repository.UserRepository;
import com.microservices.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of(User.Role.USER));
        }

        return userRepository.save(user);
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + loginRequest.getUsername()));

        if (!user.getEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        List<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), roles);
        LocalDateTime expiresAt = jwtUtil.getExpirationFromToken(token);

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), expiresAt);
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    if (userDetails.getFirstName() != null) {
                        user.setFirstName(userDetails.getFirstName());
                    }
                    if (userDetails.getLastName() != null) {
                        user.setLastName(userDetails.getLastName());
                    }
                    if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
                        if (userRepository.existsByEmail(userDetails.getEmail())) {
                            throw new RuntimeException("Email already exists: " + userDetails.getEmail());
                        }
                        user.setEmail(userDetails.getEmail());
                    }
                    if (userDetails.getEnabled() != null) {
                        user.setEnabled(userDetails.getEnabled());
                    }
                    if (userDetails.getRoles() != null) {
                        user.setRoles(userDetails.getRoles());
                    }
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getUserFromToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}