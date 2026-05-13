package com.inventory.analysis;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class AbcAnalyzer {

    private static final double THRESHOLD_A = 0.80; // до 80% обороту → клас A
    private static final double THRESHOLD_B = 0.95; // до 95% обороту → клас B

    /**
     * Приймає Map: productId → оборот за період.
     * Повертає Map: productId → клас ("A", "B" або "C").
     */
    public Map<Long, String> analyze(Map<Long, BigDecimal> revenueByProduct) {
        if (revenueByProduct == null || revenueByProduct.isEmpty()) {
            return Collections.emptyMap();
        }

        BigDecimal total = revenueByProduct.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            // Усі нулі — все клас C
            Map<Long, String> result = new LinkedHashMap<>();
            revenueByProduct.keySet().forEach(id -> result.put(id, "C"));
            return result;
        }

        // Сортуємо за оборотом від найбільшого
        List<Map.Entry<Long, BigDecimal>> sorted = new ArrayList<>(revenueByProduct.entrySet());
        sorted.sort(Map.Entry.<Long, BigDecimal>comparingByValue().reversed());

        Map<Long, String> result = new LinkedHashMap<>();
        BigDecimal cumulative = BigDecimal.ZERO;

        for (Map.Entry<Long, BigDecimal> entry : sorted) {
            cumulative = cumulative.add(entry.getValue());
            double share = cumulative
                .divide(total, 4, RoundingMode.HALF_UP)
                .doubleValue();

            String cls;
if (share <= THRESHOLD_A || result.isEmpty()) {
    // result.isEmpty() — перший товар у відсортованому списку
    // завжди клас A, навіть якщо він один і його частка = 100%
    cls = "A";
} else if (share <= THRESHOLD_B) {
    cls = "B";
} else {
    cls = "C";
}
            result.put(entry.getKey(), cls);
        }

        return result;
    }

    /**
     * Розраховує частку кожного товару у загальному обороті (0..1).
     */
    public Map<Long, BigDecimal> calcRevenueShares(Map<Long, BigDecimal> revenueByProduct) {
        BigDecimal total = revenueByProduct.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyMap();
        }

        Map<Long, BigDecimal> shares = new LinkedHashMap<>();
        revenueByProduct.forEach((id, rev) ->
            shares.put(id, rev.divide(total, 4, RoundingMode.HALF_UP)));
        return shares;
    }
}