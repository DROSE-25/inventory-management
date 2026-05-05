package com.inventory.model;

import com.inventory.model.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {

    @NotBlank @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Email @NotBlank @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private Boolean isActive = true;
}
