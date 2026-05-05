// SupplierResponse.java
package com.inventory.dto.response;
 
import lombok.*;
import java.math.BigDecimal;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private Short leadTimeDays;
    private BigDecimal minOrderAmount;
    private Boolean isActive;
}
