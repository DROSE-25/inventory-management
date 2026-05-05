package com.inventory.dto.response;
 
import lombok.*;
import java.math.BigDecimal;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLevelResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal quantity;
    private BigDecimal reorderPoint;
    private BigDecimal safetyStock;
    private BigDecimal eoq;
    private boolean belowReorderPoint;
}
