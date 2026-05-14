package com.inventory.controller;

import com.inventory.model.User;
import com.inventory.model.enums.UserRole;
import com.inventory.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Управління користувачами")
public class UserController {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Список всіх користувачів")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @Operation(summary = "Створити нового користувача")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> create(@RequestBody CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setIsActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Operation(summary = "Деактивувати користувача")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setIsActive(false);
            u.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(u);
        });
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Активувати користувача")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setIsActive(true);
            u.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(u);
        });
        return ResponseEntity.ok().build();
    }

    @lombok.Data
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private String role;
    }
}