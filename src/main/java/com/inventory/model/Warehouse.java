package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "warehouses")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Warehouse {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 255)
    private String address;

    @Positive
    @Column(precision = 12, scale = 2)
    private BigDecimal capacity;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "company_id", nullable = false)
    private Long companyId;
}
