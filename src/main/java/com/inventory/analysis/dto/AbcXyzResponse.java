package com.inventory.analysis.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AbcXyzResponse {

    private Long   productId;
    private String productName;
    private String sku;

    private String abcClass;       // "A", "B", "C"
    private String xyzClass;       // "X", "Y", "Z"
    private String combinedClass;  // "AX", "BY", "CZ" ...

    private BigDecimal revenue;
    private BigDecimal revenueShare;
    private BigDecimal cv;

    private String recommendation; // текстова рекомендація для UI
}