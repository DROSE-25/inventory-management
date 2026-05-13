package com.inventory.optimization;

import org.springframework.stereotype.Component;

/**
 * EOQ Calculator — формула Вілсона.
 * EOQ = sqrt(2 * D * S / H)
 * D — річний попит (units/year)
 * S — вартість одного замовлення (ordering cost)
 * H — вартість зберігання одиниці за рік (holding cost)
 */
@Component
public class EOQCalculator {

    /**
     * @param annualDemand     D: річна кількість одиниць
     * @param orderingCost     S: вартість одного замовлення (грн)
     * @param holdingCostPerUnit H: вартість зберігання 1 од. за рік (грн)
     * @return оптимальний розмір замовлення (округлений до цілого)
     */
    public int calculate(double annualDemand, double orderingCost, double holdingCostPerUnit) {
        if (annualDemand <= 0 || orderingCost <= 0 || holdingCostPerUnit <= 0) {
            throw new IllegalArgumentException(
                "All EOQ parameters must be positive. Got: D=" + annualDemand
                + ", S=" + orderingCost + ", H=" + holdingCostPerUnit);
        }
        double eoq = Math.sqrt((2 * annualDemand * orderingCost) / holdingCostPerUnit);
        return (int) Math.ceil(eoq); // округлюємо вгору — краще трохи більше ніж дефіцит
    }

    /**
     * Зручний метод: якщо відомий місячний попит (не річний).
     */
    public int calculateFromMonthlyDemand(
            double monthlyDemand, double orderingCost, double holdingCostPerUnit) {
        return calculate(monthlyDemand * 12, orderingCost, holdingCostPerUnit);
    }

    /**
     * Оптимальна кількість замовлень на рік: N = D / EOQ
     */
    public double optimalOrdersPerYear(
            double annualDemand, double orderingCost, double holdingCostPerUnit) {
        return annualDemand / calculate(annualDemand, orderingCost, holdingCostPerUnit);
    }

    /**
     * Загальна мінімальна вартість запасів на рік: TC = (D/EOQ)*S + (EOQ/2)*H
     */
    public double totalAnnualCost(
            double annualDemand, double orderingCost, double holdingCostPerUnit) {
        int eoq = calculate(annualDemand, orderingCost, holdingCostPerUnit);
        double orderingTotal = (annualDemand / eoq) * orderingCost;
        double holdingTotal  = (eoq / 2.0) * holdingCostPerUnit;
        return orderingTotal + holdingTotal;
    }
}
