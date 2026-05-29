package com.inventory.controller;

import com.inventory.dto.*;
import com.inventory.model.Company;
import com.inventory.model.User;
import com.inventory.model.enums.UserRole;
import com.inventory.repository.CompanyRepository;
import com.inventory.repository.UserRepository;
import com.inventory.security.JwtService;
import com.inventory.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Автентифікація та реєстрація")
public class AuthController {

    private final AuthenticationManager    authenticationManager;
    private final JwtService               jwtService;
    private final UserDetailsServiceImpl   userDetailsService;
    private final UserRepository           userRepository;
    private final CompanyRepository        companyRepository;
    private final PasswordEncoder          passwordEncoder;

    // ── Логін ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Увійти та отримати JWT токен")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(LoginResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(3600000L)
            .username(userDetails.getUsername())
            .role(userDetails.getAuthorities().stream()
                .findFirst().map(Object::toString).orElse(""))
            .build());
    }

    // ── Реєстрація ────────────────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Зареєструвати нову компанію та першого адміністратора")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

        // Перевірка унікальності логіна і email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Користувач з таким логіном вже існує");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Користувач з таким email вже існує");
        }
        if (companyRepository.existsByName(request.getCompanyName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Компанія з такою назвою вже зареєстрована");
        }

        // Створюємо компанію
        Company company = companyRepository.save(
            Company.builder().name(request.getCompanyName()).build()
        );

        // Створюємо першого адміна компанії
        User admin = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.ADMIN)
            .isActive(true)
            .build();

        admin.setCompanyId(company.getId());
        admin.setFullName(request.getFullName());
        userRepository.save(admin);

        // Генеруємо JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            RegisterResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .username(admin.getUsername())
                .role("ROLE_ADMIN")
                .companyName(company.getName())
                .message("Реєстрацію успішно завершено! Ласкаво просимо, " + request.getFullName() + "!")
                .build()
        );
    }

    // ── Перевірка доступності логіна ─────────────────────────────────────────

    @GetMapping("/check-username")
    @Operation(summary = "Перевірити чи логін вільний")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(!userRepository.existsByUsername(username));
    }

    @GetMapping("/check-email")
    @Operation(summary = "Перевірити чи email вільний")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(!userRepository.existsByEmail(email));
    }
}
