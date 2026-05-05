package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sales")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Sale {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @NotNull
    @Column(nullable = false)
    private LocalDate saleDate;

    @Positive
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    private OffsetDateTime createdAt;
}
