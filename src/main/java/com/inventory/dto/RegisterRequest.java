package com.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Назва компанії обов'язкова")
    @Size(min = 2, max = 200, message = "Назва компанії: від 2 до 200 символів")
    private String companyName;

    @NotBlank(message = "Ім'я обов'язкове")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Логін обов'язковий")
    @Size(min = 3, max = 50, message = "Логін: від 3 до 50 символів")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Логін може містити лише латинські літери, цифри та _")
    private String username;

    @NotBlank(message = "Email обов'язковий")
    @Email(message = "Невірний формат email")
    private String email;

    @NotBlank(message = "Пароль обов'язковий")
    @Size(min = 6, max = 100, message = "Пароль: мінімум 6 символів")
    private String password;
}
