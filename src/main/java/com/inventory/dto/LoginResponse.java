package com.inventory.dto;
 
import lombok.*;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String username;
    private String role;
}
