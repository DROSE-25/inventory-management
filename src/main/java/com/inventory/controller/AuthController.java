package com.inventory.controller;
 
import com.inventory.dto.LoginRequest;
import com.inventory.dto.LoginResponse;
import com.inventory.security.JwtService;
import com.inventory.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Автентифікація")
public class AuthController {
 
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
 
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
            .expiresIn(3600000)
            .username(userDetails.getUsername())
            .role(userDetails.getAuthorities().stream()
                .findFirst().map(Object::toString).orElse(""))
            .build());
    }
}
