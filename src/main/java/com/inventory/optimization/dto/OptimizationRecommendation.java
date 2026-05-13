package com.inventory.optimization.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptimizationRecommendation {

    private Long   productId;
    private String productName;
    private String forecastMethod;     // який метод прогнозування використано

    // Прогнозні дані
    private double forecastedMonthlyDemand;  // прогноз попиту на наступний місяць
    private double forecastMape;             // точність прогнозу (%)

    // Розрахунки оптимізації
    private int    eoq;              // оптимальний розмір замовлення (одиниць)
    private int    safetyStock;      // страховий запас (одиниць)
    private int    reorderPoint;     // точка перезамовлення (одиниць)
    private int    currentStock;     // поточний залишок на складі

    // Рекомендація
    private boolean needsReorder;   // чи потрібно замовляти ЗАРАЗ
    private String  recommendation; // текстова рекомендація

    // Економіка
    private double totalAnnualCost; // мінімальна річна вартість запасів (грн)
}
