package com.inventory.model;

import com.inventory.model.enums.PoStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PoStatus status = PoStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate orderDate = LocalDate.now();

    private LocalDate expectedDelivery;
    private LocalDate actualDelivery;

    @PositiveOrZero
    @Column(precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();
}

