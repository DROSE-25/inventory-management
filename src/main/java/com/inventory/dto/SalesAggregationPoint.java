package com.inventory.dto;
 
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesAggregationPoint {
    private LocalDate periodStart;   // початок дня/тижня/місяця
    private BigDecimal totalQuantity;
    private BigDecimal totalRevenue;
}
