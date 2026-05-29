package com.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String accessToken;
    private String tokenType;
    private Long   expiresIn;
    private String username;
    private String role;
    private String companyName;
    private String message;
}
