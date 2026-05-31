package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "suppliers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Supplier {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 150)
    @Column(nullable = false)
    private String name;

    @Size(max = 100)
    private String contactPerson;

    @Size(max = 30)
    private String phone;

    @Email @Size(max = 100)
    private String email;

    @Positive
    @Column(nullable = false)
    private Short leadTimeDays;

    @PositiveOrZero
    private BigDecimal minOrderAmount;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "company_id", nullable = false)
    private Long companyId;
}
