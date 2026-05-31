package com.inventory.controller;

import com.inventory.model.User;
import com.inventory.model.enums.UserRole;
import com.inventory.repository.UserRepository;
import com.inventory.security.SecurityUtils;
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
    private final SecurityUtils   securityUtils;

    record UserDto(Long id, String username, String email, String role, Boolean isActive) {}

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getEmail(),
            u.getRole().name(), u.getIsActive());
    }

    @Operation(summary = "Список користувачів компанії")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAll() {
        Long companyId = securityUtils.getCurrentCompanyId();
        // Повертаємо лише користувачів цієї ж компанії
        return ResponseEntity.ok(
            userRepository.findAll().stream()
                .filter(u -> companyId.equals(u.getCompanyId()))
                .map(this::toDto)
                .toList());
    }

    @Operation(summary = "Створити нового користувача в межах компанії")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> create(@RequestBody CreateUserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Long companyId = securityUtils.getCurrentCompanyId();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setIsActive(true);
        user.setCompanyId(companyId);   // <-- прив'язуємо до тієї ж компанії
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return ResponseEntity.ok(toDto(userRepository.save(user)));
    }

    @Operation(summary = "Деактивувати користувача")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        Long companyId = securityUtils.getCurrentCompanyId();
        userRepository.findById(id)
            .filter(u -> companyId.equals(u.getCompanyId()))
            .ifPresent(u -> {
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
        Long companyId = securityUtils.getCurrentCompanyId();
        userRepository.findById(id)
            .filter(u -> companyId.equals(u.getCompanyId()))
            .ifPresent(u -> {
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
