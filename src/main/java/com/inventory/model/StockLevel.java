package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stock_levels",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id","warehouse_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockLevel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(precision = 12, scale = 3)
    private BigDecimal reorderPoint;

    @PositiveOrZero
    @Column(precision = 12, scale = 3)
    private BigDecimal safetyStock;

    @Positive
    @Column(precision = 12, scale = 3)
    private BigDecimal eoq;

    private OffsetDateTime updatedAt;
}

