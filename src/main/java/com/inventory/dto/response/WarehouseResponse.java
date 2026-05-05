package com.inventory.dto.response;
 
import lombok.*;
import java.math.BigDecimal;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarehouseResponse {
    private Long id;
    private String name;
    private String address;
    private BigDecimal capacity;
    private Boolean isActive;
}
