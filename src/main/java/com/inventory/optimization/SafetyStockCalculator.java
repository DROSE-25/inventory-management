package com.inventory.optimization;

import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Safety Stock = z * stdDev(demand) * sqrt(leadTimeDays)
 */
@Component
public class SafetyStockCalculator {

    /**
     * @param demandHistory   список щоденних/тижневих продажів за останній період
     * @param leadTimeDays    час виконання замовлення (дні)
     * @param serviceLevel    рівень обслуговування: 0.90, 0.95 або 0.99
     * @return страховий запас (округлений вгору)
     */
    public int calculate(List<Double> demandHistory, int leadTimeDays, double serviceLevel) {
        if (demandHistory == null || demandHistory.size() < 2) {
            throw new IllegalArgumentException(
                "Need at least 2 demand observations to calculate safety stock");
        }
        if (leadTimeDays <= 0) {
            throw new IllegalArgumentException("Lead time must be positive");
        }

        double z         = getZScore(serviceLevel);
        double stdDev    = calculateStdDev(demandHistory);
        double safetyStock = z * stdDev * Math.sqrt(leadTimeDays);

        return (int) Math.ceil(safetyStock);
    }

    /**
     * Перетворює рівень обслуговування на z-коефіцієнт (нормальний розподіл).
     * Стандартні значення: 90%→1.28, 95%→1.65, 99%→2.33
     */
    public double getZScore(double serviceLevel) {
        if (serviceLevel >= 0.99) return 2.33;
        if (serviceLevel >= 0.97) return 1.88;
        if (serviceLevel >= 0.95) return 1.65;
        if (serviceLevel >= 0.90) return 1.28;
        if (serviceLevel >= 0.85) return 1.04;
        return 1.28; // default: 90%
    }

    /**
     * Стандартне відхилення вибірки (sample std dev, ділимо на N-1).
     */
    private double calculateStdDev(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .sum() / (values.size() - 1);
        return Math.sqrt(variance);
    }
}
