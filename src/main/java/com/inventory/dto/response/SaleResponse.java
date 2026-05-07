package com.inventory.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class SaleResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private LocalDate saleDate;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
}