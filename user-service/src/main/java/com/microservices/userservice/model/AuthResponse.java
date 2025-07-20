package com.microservices.userservice.model;

import java.time.LocalDateTime;
import java.util.Set;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private Set<User.Role> roles;
    private LocalDateTime expiresAt;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String username, String email, Set<User.Role> roles, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<User.Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<User.Role> roles) {
        this.roles = roles;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}