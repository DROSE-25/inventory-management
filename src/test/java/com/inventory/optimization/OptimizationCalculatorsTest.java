package com.inventory.optimization;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OptimizationCalculatorsTest {

    private EOQCalculator            eoq;
    private SafetyStockCalculator    ss;
    private ReorderPointCalculator   rop;

    @BeforeEach
    void setUp() {
        eoq = new EOQCalculator();
        ss  = new SafetyStockCalculator();
        rop = new ReorderPointCalculator();
    }

    // ─── EOQ Tests ─────────────────────────────────────────────────
    @Test
    void eoq_classicExample_returns69() {
        // D=1200, S=200, H=100 → EOQ = sqrt(480000/100) = sqrt(4800) ≈ 69
        int result = eoq.calculate(1200, 200, 100);
        assertEquals(70, result); // ceil(69.28...) = 70
    }

    @Test
    void eoq_higherOrderingCost_largerEOQ() {
        // При вищій вартості замовлення EOQ зростає
        int small = eoq.calculate(1200, 100, 100);
        int large = eoq.calculate(1200, 400, 100);
        assertTrue(large > small, "Higher S should produce larger EOQ");
    }

    @Test
    void eoq_higherHoldingCost_smallerEOQ() {
        // При вищій вартості зберігання EOQ зменшується
        int small = eoq.calculate(1200, 200, 200);
        int large = eoq.calculate(1200, 200, 50);
        assertTrue(large > small, "Lower H should produce larger EOQ");
    }

    @Test
    void eoq_negativeParams_throws() {
        assertThrows(IllegalArgumentException.class, () -> eoq.calculate(-100, 200, 100));
        assertThrows(IllegalArgumentException.class, () -> eoq.calculate(1200, 0, 100));
        assertThrows(IllegalArgumentException.class, () -> eoq.calculate(1200, 200, -1));
    }

    // ─── Safety Stock Tests ────────────────────────────────────────
    @Test
    void ss_stableData_lowSafetyStock() {
        // Якщо попит стабільний (stdDev ≈ 0) — страховий запас мінімальний
        List<Double> stable = List.of(100.0, 100.0, 100.0, 100.0, 100.0,
                                      100.0, 100.0, 100.0, 100.0, 100.0);
        int result = ss.calculate(stable, 7, 0.95);
        assertEquals(0, result, "Stable demand → safety stock = 0");
    }

    @Test
    void ss_higherServiceLevel_higgerSS() {
        List<Double> demand = List.of(80.0, 120.0, 90.0, 110.0, 95.0,
                                      105.0, 85.0, 115.0, 92.0, 108.0);
        int ss90 = ss.calculate(demand, 7, 0.90);
        int ss99 = ss.calculate(demand, 7, 0.99);
        assertTrue(ss99 > ss90, "Higher service level → more safety stock");
    }

    @Test
    void ss_longerLeadTime_higgerSS() {
        List<Double> demand = List.of(80.0, 120.0, 90.0, 110.0, 95.0,
                                      105.0, 85.0, 115.0, 92.0, 108.0);
        int ss7  = ss.calculate(demand, 7, 0.95);
        int ss21 = ss.calculate(demand, 21, 0.95);
        assertTrue(ss21 > ss7, "Longer lead time → more safety stock");
    }

    @Test
    void ss_tooFewPoints_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> ss.calculate(List.of(100.0), 7, 0.95));
    }

    // ─── ROP Tests ─────────────────────────────────────────────────
    @Test
    void rop_classicExample_returns53() {
        // d=4 од/день, L=7 днів, SS=25 → ROP = 4*7 + 25 = 53
        int result = rop.calculate(4.0, 7, 25);
        assertEquals(53, result);
    }

    @Test
    void rop_zeroSafetyStock_equalsDeliveryDemand() {
        // Без страхового запасу ROP = d * L
        int result = rop.calculate(10.0, 5, 0);
        assertEquals(50, result);
    }

    @Test
    void rop_invalidParams_throws() {
        assertThrows(IllegalArgumentException.class, () -> rop.calculate(4.0, 0, 25));
        assertThrows(IllegalArgumentException.class, () -> rop.calculate(-1.0, 7, 25));
    }

    // ─── Integration: EOQ + SS + ROP ──────────────────────────────
    @Test
    void fullFlow_givenProduct_returnsConsistentRecommendation() {
        // Симуляція реального сценарію:
        // Попит: середньо 100 на місяць, стд. відхилення ≈ 15
        List<Double> history = List.of(85.0, 110.0, 95.0, 115.0, 90.0,
                                       105.0, 88.0, 112.0, 97.0, 103.0);
        int leadTime = 7; // 1 тиждень

        int safetyStock = ss.calculate(history, leadTime, 0.95);
        int eoqValue    = eoq.calculate(1200, 200, 100); // D=1200, S=200, H=100
        int ropValue    = rop.calculateFromMonthlyDemand(100.0, leadTime, safetyStock);

        assertTrue(safetyStock >= 0,  "Safety stock must be non-negative");
        assertTrue(eoqValue > 0,       "EOQ must be positive");
        assertTrue(ropValue > safetyStock,
            "ROP must be greater than safety stock");
    }
}
