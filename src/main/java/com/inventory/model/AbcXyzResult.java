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
    private String combinedClass;

    @Column(precision = 14, scale = 2)
    private BigDecimal revenue;

    @Column(name = "revenue_share", precision = 6, scale = 4)
    private BigDecimal revenueShare;

    @Column(precision = 8, scale = 2)
    private BigDecimal cv;

    private LocalDate periodFrom;
    private LocalDate periodTo;

    private OffsetDateTime calculatedAt;

    // companyId — для ізоляції даних між компаніями
    // значення береться з product.companyId при збереженні
    @Column(name = "company_id")
    private Long companyId;
}