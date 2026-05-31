package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String sku;

    @NotBlank @Size(max = 200)
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 20)
    private String unitOfMeasure = "шт";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal orderingCost = BigDecimal.valueOf(100);

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal holdingCostRate = BigDecimal.valueOf(0.2);

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal serviceLevel = BigDecimal.valueOf(0.95);

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "company_id", nullable = false)
    private Long companyId;
}
