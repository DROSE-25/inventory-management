package com.inventory.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EOQCalculator — тести формули Вілсона")
class EOQCalculatorTest {

    private EOQCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EOQCalculator();
    }

    @Test
    @DisplayName("Стандартний розрахунок EOQ: D=1000, S=50, H=2 → очікується 224")
    void calculate_standardValues_returnsExpectedEOQ() {
        // EOQ = sqrt(2 * 1000 * 50 / 2) = sqrt(50000) ≈ 223.6 → ceil = 224
        int result = calculator.calculate(1000, 50, 2);
        assertEquals(224, result);
    }

    @Test
    @DisplayName("EOQ при мінімальних значеннях: D=1, S=1, H=1 → повертає 2")
    void calculate_minValues_returnsTwo() {
        // sqrt(2) ≈ 1.41 → ceil = 2
        int result = calculator.calculate(1, 1, 1);
        assertEquals(2, result);
    }

    @Test
    @DisplayName("EOQ завжди заокруглюється вгору (ceil)")
    void calculate_alwaysRoundsUp() {
        // sqrt(2 * 100 * 10 / 3) = sqrt(666.7) ≈ 25.82 → ceil = 26
        int result = calculator.calculate(100, 10, 3);
        assertEquals(26, result);
    }

    @Test
    @DisplayName("calculateFromMonthlyDemand: місячний → річний попит × 12")
    void calculateFromMonthlyDemand_multipliesBy12() {
        int fromMonthly = calculator.calculateFromMonthlyDemand(100, 50, 2);
        int fromAnnual  = calculator.calculate(1200, 50, 2);
        assertEquals(fromAnnual, fromMonthly);
    }

    @Test
    @DisplayName("optimalOrdersPerYear: D / EOQ")
    void optimalOrdersPerYear_returnsCorrectValue() {
        double orders = calculator.optimalOrdersPerYear(1000, 50, 2);
        // EOQ=224, N = 1000/224 ≈ 4.46
        assertTrue(orders > 4.0 && orders < 5.0);
    }

    @Test
    @DisplayName("totalAnnualCost — число більше нуля")
    void totalAnnualCost_isPositive() {
        double cost = calculator.totalAnnualCost(1000, 50, 2);
        assertTrue(cost > 0);
    }

    @Test
    @DisplayName("IllegalArgumentException при D=0")
    void calculate_zeroDemand_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(0, 50, 2));
    }

    @Test
    @DisplayName("IllegalArgumentException при від'ємному S")
    void calculate_negativeOrderingCost_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(1000, -1, 2));
    }

    @Test
    @DisplayName("IllegalArgumentException при H=0")
    void calculate_zeroHoldingCost_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(1000, 50, 0));
    }

    @Test
    @DisplayName("Симетрія: подвійний попит → EOQ збільшується в sqrt(2) разів")
    void calculate_doubledDemand_increasesEOQBySqrt2() {
        int eoq1 = calculator.calculate(1000, 50, 2);
        int eoq2 = calculator.calculate(2000, 50, 2);
        double ratio = (double) eoq2 / eoq1;
        assertEquals(Math.sqrt(2), ratio, 0.05);
    }
}
