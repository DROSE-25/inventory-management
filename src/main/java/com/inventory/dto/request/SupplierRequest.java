package com.inventory.dto.request;
 
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierRequest {
    @NotBlank @Size(max = 150)
    private String name;
    @Size(max = 100)
    private String contactPerson;
    @Size(max = 30)
    private String phone;
    @Email @Size(max = 100)
    private String email;
    @NotNull @Positive
    private Short leadTimeDays;
    @PositiveOrZero
    private BigDecimal minOrderAmount;
}
