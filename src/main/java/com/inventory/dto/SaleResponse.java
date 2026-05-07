package com.inventory.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private Long productId;
    private String productName; // Полезно добавить имя товара для фронтенда
    private Long warehouseId;
    private LocalDate saleDate;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice; // quantity * unitPrice
}
