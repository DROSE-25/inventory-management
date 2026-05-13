package com.inventory.optimization;

import org.springframework.stereotype.Component;

/**
 * ROP = d * L + SS
 * d — середньоденний попит, L — lead time (дні), SS — страховий запас
 */
@Component
public class ReorderPointCalculator {

    /**
     * @param dailyDemand    d: середньоденний попит (од./день)
     * @param leadTimeDays   L: lead time у днях
     * @param safetyStock    SS: страховий запас (вже розрахований)
     * @return точка перезамовлення (ціле число, округлене вгору)
     */
    public int calculate(double dailyDemand, int leadTimeDays, int safetyStock) {
        if (dailyDemand < 0 || leadTimeDays <= 0 || safetyStock < 0) {
            throw new IllegalArgumentException(
                "Invalid ROP parameters: d=" + dailyDemand
                + ", L=" + leadTimeDays + ", SS=" + safetyStock);
        }
        double rop = dailyDemand * leadTimeDays + safetyStock;
        return (int) Math.ceil(rop);
    }

    /**
     * Зручний метод: якщо відомий місячний попит (30 днів).
     */
    public int calculateFromMonthlyDemand(
            double monthlyDemand, int leadTimeDays, int safetyStock) {
        double dailyDemand = monthlyDemand / 30.0;
        return calculate(dailyDemand, leadTimeDays, safetyStock);
    }
}
