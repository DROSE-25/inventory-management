package com.inventory.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SafetyStockCalculator — тести SS = z * σ * sqrt(L)")
class SafetyStockCalculatorTest {

    private SafetyStockCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SafetyStockCalculator();
    }

    @Test
    @DisplayName("SS при 95% рівні сервісу більший ніж при 90%")
    void calculate_higherServiceLevel_returnsHigherSS() {
        List<Double> demand = Arrays.asList(10.0, 12.0, 8.0, 15.0, 9.0, 11.0);
        int ss90 = calculator.calculate(demand, 7, 0.90);
        int ss95 = calculator.calculate(demand, 7, 0.95);
        assertTrue(ss95 > ss90);
    }

    @Test
    @DisplayName("SS при 99% рівні сервісу — максимальний")
    void calculate_99percentLevel_isHigherThan95() {
        List<Double> demand = Arrays.asList(10.0, 12.0, 8.0, 15.0, 9.0, 11.0);
        int ss95 = calculator.calculate(demand, 7, 0.95);
        int ss99 = calculator.calculate(demand, 7, 0.99);
        assertTrue(ss99 > ss95);
    }

    @Test
    @DisplayName("SS зростає зі збільшенням lead time")
    void calculate_longerLeadTime_returnsHigherSS() {
        List<Double> demand = Arrays.asList(10.0, 12.0, 8.0, 15.0, 9.0, 11.0);
        int ss7  = calculator.calculate(demand, 7, 0.95);
        int ss14 = calculator.calculate(demand, 14, 0.95);
        assertTrue(ss14 > ss7);
    }

    @Test
    @DisplayName("Стабільний попит (нульове відхилення) → SS = 0")
    void calculate_stableDemand_returnsZero() {
        List<Double> demand = Arrays.asList(10.0, 10.0, 10.0, 10.0, 10.0);
        int ss = calculator.calculate(demand, 7, 0.95);
        assertEquals(0, ss);
    }

    @Test
    @DisplayName("getZScore: 90% → 1.28")
    void getZScore_90percent_returns1_28() {
        assertEquals(1.28, calculator.getZScore(0.90), 0.001);
    }

    @Test
    @DisplayName("getZScore: 95% → 1.65")
    void getZScore_95percent_returns1_65() {
        assertEquals(1.65, calculator.getZScore(0.95), 0.001);
    }

    @Test
    @DisplayName("getZScore: 99% → 2.33")
    void getZScore_99percent_returns2_33() {
        assertEquals(2.33, calculator.getZScore(0.99), 0.001);
    }

    @Test
    @DisplayName("SS завжди невід'ємний")
    void calculate_alwaysNonNegative() {
        List<Double> demand = Arrays.asList(5.0, 8.0, 3.0, 12.0, 7.0);
        int ss = calculator.calculate(demand, 3, 0.95);
        assertTrue(ss >= 0);
    }

    @Test
    @DisplayName("IllegalArgumentException при менше 2 спостережень")
    void calculate_lessThan2Points_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(List.of(10.0), 7, 0.95));
    }

    @Test
    @DisplayName("IllegalArgumentException при null")
    void calculate_nullHistory_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(null, 7, 0.95));
    }

    @Test
    @DisplayName("IllegalArgumentException при L=0")
    void calculate_zeroLeadTime_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> calculator.calculate(Arrays.asList(10.0, 12.0), 0, 0.95));
    }
}
