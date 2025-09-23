package com.apimock.entity.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserConstructorWithParameters() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedpassword";

        // When
        User newUser = new User(username, email, passwordHash);

        // Then
        assertThat(newUser.getUsername()).isEqualTo(username);
        assertThat(newUser.getEmail()).isEqualTo(email);
        assertThat(newUser.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(newUser.getRole()).isEqualTo(UserRole.DEVELOPER);
        assertThat(newUser.getStatus()).isEqualTo(1);
        assertThat(newUser.getFailedLoginAttempts()).isEqualTo(0);
    }

    @Test
    void testUserDefaultValues() {
        // When
        User newUser = new User();

        // Then
        assertThat(newUser.getRole()).isEqualTo(UserRole.DEVELOPER);
        assertThat(newUser.getStatus()).isEqualTo(1);
        assertThat(newUser.getFailedLoginAttempts()).isEqualTo(0);
    }

    @Test
    void testGetPasswordReturnsPasswordHash() {
        // Given
        String passwordHash = "hashedpassword123";
        user.setPasswordHash(passwordHash);

        // When & Then
        assertThat(user.getPassword()).isEqualTo(passwordHash);
        assertThat(user.getPassword()).isEqualTo(user.getPasswordHash());
    }

    @Test
    void testIsActiveWhenStatusIsOne() {
        // Given
        user.setStatus(1);

        // When & Then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void testIsNotActiveWhenStatusIsZero() {
        // Given
        user.setStatus(0);

        // When & Then
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void testIsNotLockedWhenLockedUntilIsNull() {
        // Given
        user.setLockedUntil(null);

        // When & Then
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    void testIsNotLockedWhenLockedUntilIsInPast() {
        // Given
        user.setLockedUntil(LocalDateTime.now().minusHours(1));

        // When & Then
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    void testIsLockedWhenLockedUntilIsInFuture() {
        // Given
        user.setLockedUntil(LocalDateTime.now().plusHours(1));

        // When & Then
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    void testSettersAndGetters() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedpassword";
        UserRole role = UserRole.PROJECT_ADMIN;
        Integer status = 0;
        LocalDateTime lastLoginAt = LocalDateTime.now();
        Integer failedLoginAttempts = 3;
        LocalDateTime lockedUntil = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setStatus(status);
        user.setLastLoginAt(lastLoginAt);
        user.setFailedLoginAttempts(failedLoginAttempts);
        user.setLockedUntil(lockedUntil);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getStatus()).isEqualTo(status);
        assertThat(user.getLastLoginAt()).isEqualTo(lastLoginAt);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(failedLoginAttempts);
        assertThat(user.getLockedUntil()).isEqualTo(lockedUntil);
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
        assertThat(user.getUpdatedAt()).isEqualTo(updatedAt);
    }
}