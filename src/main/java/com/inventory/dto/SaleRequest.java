package com.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    @NotNull(message = "ID товара обязателен")
    private Long productId;

    @NotNull(message = "ID склада обязателен")
    private Long warehouseId;

    @NotNull(message = "Дата продажи обязательна")
    @PastOrPresent(message = "Дата продажи не может быть в будущем")
    private LocalDate saleDate;

    @NotNull(message = "Количество обязательно")
    @Positive(message = "Количество должно быть больше 0")
    private BigDecimal quantity;

    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть больше 0")
    private BigDecimal unitPrice;
}