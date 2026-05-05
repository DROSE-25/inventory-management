package com.inventory.dto.request;
 
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductRequest {
 
    @NotBlank @Size(max = 50)
    private String sku;
 
    @NotBlank @Size(max = 200)
    private String name;
 
    @NotNull
    private Long categoryId;
 
    @NotNull
    private Long supplierId;
 
    @NotNull @Positive
    private BigDecimal unitPrice;
 
    @Size(max = 20)
    private String unitOfMeasure = "шт";
 
    @Positive
    private BigDecimal orderingCost;
 
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal holdingCostRate;
 
    @DecimalMin("0.0") @DecimalMax("1.0")
    private BigDecimal serviceLevel;
}
