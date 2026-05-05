package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Positive
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityOrdered;

    @PositiveOrZero
    @Column(precision = 12, scale = 3)
    private BigDecimal quantityReceived;

    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
}

