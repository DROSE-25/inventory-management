package com.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "abc_xyz_results")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AbcXyzResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "abc_class", nullable = false, columnDefinition = "VARCHAR(1)")
private String abcClass;

@Column(name = "xyz_class", nullable = false, columnDefinition = "VARCHAR(1)")
private String xyzClass;

@Column(name = "combined_class", nullable = false, columnDefinition = "VARCHAR(2)")
private String combinedClass; // "AX", "BY", "CZ" ...

    @Column(precision = 14, scale = 2)
    private BigDecimal revenue;     // оборот за період

    @Column(name = "revenue_share", precision = 6, scale = 4)
    private BigDecimal revenueShare; // частка від загального (0..1)

    @Column(precision = 8, scale = 2)
    private BigDecimal cv;          // коефіцієнт варіації (%)

    private LocalDate periodFrom;
    private LocalDate periodTo;

    private OffsetDateTime calculatedAt;
}