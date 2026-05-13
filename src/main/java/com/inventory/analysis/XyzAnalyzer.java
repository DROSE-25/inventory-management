package com.inventory.analysis;

import com.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class XyzAnalyzer {

    private final SaleRepository saleRepository;

    private static final double THRESHOLD_X = 10.0; // CV < 10% → X
    private static final double THRESHOLD_Y = 25.0; // CV < 25% → Y, інше → Z

    /**
     * Розраховує XYZ-клас для одного товару.
     * CV рахується по МІСЯЧНИХ агрегатах (не денних!).
     */
    public String analyze(Long productId) {
        // Використовуємо наявний метод з SaleRepository (findMonthlyDemand)
        List<Object[]> monthly = saleRepository.findMonthlyDemand(productId);

        if (monthly == null || monthly.size() < 2) {
            return "Z"; // недостатньо даних — вважаємо нестабільним
        }

        double[] values = monthly.stream()
            .mapToDouble(row -> ((Number) row[1]).doubleValue())
            .toArray();

        double mean = Arrays.stream(values).average().orElse(0);

        if (mean == 0) {
            return "Z"; // товар не продавався — edge case
        }

        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);

        double cv = (Math.sqrt(variance) / mean) * 100.0;

        if (cv < THRESHOLD_X) return "X";
        if (cv < THRESHOLD_Y) return "Y";
        return "Z";
    }

    /**
     * Повертає числове значення CV (для збереження в БД).
     */
    public double calcCv(Long productId) {
        List<Object[]> monthly = saleRepository.findMonthlyDemand(productId);

        if (monthly == null || monthly.size() < 2) return 999.0;

        double[] values = monthly.stream()
            .mapToDouble(row -> ((Number) row[1]).doubleValue())
            .toArray();

        double mean = Arrays.stream(values).average().orElse(0);
        if (mean == 0) return 999.0;

        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);

        return (Math.sqrt(variance) / mean) * 100.0;
    }
}