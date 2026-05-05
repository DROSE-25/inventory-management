package com.inventory.dto.response;
 
import lombok.*;
import java.math.BigDecimal;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal unitPrice;
    private String unitOfMeasure;
    private BigDecimal orderingCost;
    private BigDecimal holdingCostRate;
    private BigDecimal serviceLevel;
    private Boolean isActive;
}
