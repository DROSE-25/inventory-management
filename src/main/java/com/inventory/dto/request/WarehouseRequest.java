package com.inventory.dto.request;
 
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseRequest {
    @NotBlank @Size(max = 100)
    private String name;
    @Size(max = 255)
    private String address;
    @Positive
    private BigDecimal capacity;
}
