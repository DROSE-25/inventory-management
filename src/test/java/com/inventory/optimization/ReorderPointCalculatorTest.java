package com.inventory.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReorderPointCalculator — тести ROP = d*L + SS")
class ReorderPointCalculatorTest {

    private ReorderPointCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ReorderPointCalculator();
    }

    @Test
    @DisplayName("Стандартний ROP: d=10, L=5, SS=20 → 70")
    void calculate_standardValues_returns70() {
        int result = calculator.calculate(10.0, 5, 20);
        assertEquals(70, result);
    }

    @Test
    @DisplayName("ROP без страхового запасу: d=5, L=4, SS=0 → 20")
    void calculate_noSafetyStock_returnsExactProduct() {
        int result = calculator.calculate(5.0, 4, 0);
        assertEquals(20, result);
    }

    @Test
    @DisplayName("ROP заокруглюється вгору при дробових значеннях")
    void calculate_fractionalDemand_roundsUp() {
        // 2.5 * 3 + 0 = 7.5 → ceil = 8
        int result = calculator.calculate(2.5, 3, 0);
        assertEquals(8, result);
    }

    @Test
    @DisplayName("calculateFromMonthlyDemand: ділить на 30")
    void calculateFromMonthlyDemand_dividesByThirty() {
        // d = 300/30 = 10, L=5, SS=0 → 50
        int result = calculator.calculateFromMonthlyDemand(300.0, 5, 0);
        assertEquals(50, result);
    }

    @Test
    @DisplayName("Нульовий попит повертає страховий запас")
    void calculate_zeroDemand_returnsSafetyStock() {
        int result = calculator.calculate(0.0, 7, 30);
        assertEquals(30, result);
    }

    @Test
    @DisplayName("IllegalArgumentException при L=0")
    void calculate_zeroLeadTime_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(10.0, 0, 20));
    }

    @Test
    @DisplayName("IllegalArgumentException при від'ємному d")
    void calculate_negativeDemand_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(-1.0, 5, 20));
    }

    @Test
    @DisplayName("IllegalArgumentException при від'ємному SS")
    void calculate_negativeSafetyStock_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(10.0, 5, -1));
    }

    @Test
    @DisplayName("Збільшення L пропорційно збільшує ROP")
    void calculate_doubledLeadTime_doublesROP() {
        int rop1 = calculator.calculate(10.0, 5, 0);
        int rop2 = calculator.calculate(10.0, 10, 0);
        assertEquals(rop1 * 2, rop2);
    }
}
