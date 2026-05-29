package com.inventory.forecast.evaluation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ForecastAccuracyEvaluator — тести метрик MAE/MAPE/RMSE")
class ForecastAccuracyEvaluatorTest {

    private ForecastAccuracyEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ForecastAccuracyEvaluator();
    }

    // ── MAE ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("MAE: ідеальний прогноз → 0")
    void mae_perfectForecast_returnsZero() {
        List<Double> actual    = Arrays.asList(10.0, 20.0, 30.0);
        List<Double> predicted = Arrays.asList(10.0, 20.0, 30.0);
        assertEquals(0.0, evaluator.mae(actual, predicted), 0.001);
    }

    @Test
    @DisplayName("MAE: постійне відхилення 5 → повертає 5")
    void mae_constantError_returnsError() {
        List<Double> actual    = Arrays.asList(10.0, 20.0, 30.0);
        List<Double> predicted = Arrays.asList(15.0, 25.0, 35.0);
        assertEquals(5.0, evaluator.mae(actual, predicted), 0.001);
    }

    @Test
    @DisplayName("MAE: різнонаправлені помилки → середня абсолютна")
    void mae_mixedErrors_returnsAvgAbsolute() {
        List<Double> actual    = Arrays.asList(10.0, 10.0);
        List<Double> predicted = Arrays.asList(12.0, 8.0);  // +2, -2 → MAE=2
        assertEquals(2.0, evaluator.mae(actual, predicted), 0.001);
    }

    // ── MAPE ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("MAPE: ідеальний прогноз → 0")
    void mape_perfectForecast_returnsZero() {
        List<Double> actual    = Arrays.asList(100.0, 200.0, 300.0);
        List<Double> predicted = Arrays.asList(100.0, 200.0, 300.0);
        assertEquals(0.0, evaluator.mape(actual, predicted), 0.001);
    }

    @Test
    @DisplayName("MAPE: 10% постійна помилка → повертає 10")
    void mape_tenPercentError_returnsTen() {
        List<Double> actual    = Arrays.asList(100.0, 200.0, 300.0);
        List<Double> predicted = Arrays.asList(110.0, 220.0, 330.0);
        assertEquals(10.0, evaluator.mape(actual, predicted), 0.001);
    }

    @Test
    @DisplayName("MAPE: пропускає нульові actual значення")
    void mape_zeroActual_skipsPoint() {
        // actual=0 не враховується в MAPE
        List<Double> actual    = Arrays.asList(0.0, 100.0);
        List<Double> predicted = Arrays.asList(50.0, 110.0);
        // тільки другий елемент: |100-110|/100 = 0.1 → /2 = 5%
        assertEquals(5.0, evaluator.mape(actual, predicted), 0.001);
    }

    @Test
    @DisplayName("MAPE повертає відсотки (0–100+), не частки")
    void mape_returnsPercentage_notFraction() {
        List<Double> actual    = Arrays.asList(100.0);
        List<Double> predicted = Arrays.asList(150.0); // 50% помилка
        assertEquals(50.0, evaluator.mape(actual, predicted), 0.001);
    }

    // ── RMSE ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RMSE: ідеальний прогноз → 0")
    void rmse_perfectForecast_returnsZero() {
        List<Double> actual    = Arrays.asList(10.0, 20.0, 30.0);
        List<Double> predicted = Arrays.asList(10.0, 20.0, 30.0);
        assertEquals(0.0, evaluator.rmse(actual, predicted), 0.001);
    }

    @Test
    @DisplayName("RMSE: постійна помилка 3 → повертає 3")
    void rmse_constantError3_returns3() {
        List<Double> actual    = Arrays.asList(10.0, 20.0, 30.0);
        List<Double> predicted = Arrays.asList(13.0, 23.0, 33.0);
        assertEquals(3.0, evaluator.rmse(actual, predicted), 0.001);
    }

    @Test
    @DisplayName("RMSE >= MAE завжди (квадрат посилює великі помилки)")
    void rmse_alwaysGreaterOrEqualToMAE() {
        List<Double> actual    = Arrays.asList(10.0, 20.0, 5.0, 30.0);
        List<Double> predicted = Arrays.asList(12.0, 18.0, 8.0, 25.0);
        double mae  = evaluator.mae(actual, predicted);
        double rmse = evaluator.rmse(actual, predicted);
        assertTrue(rmse >= mae);
    }

    @Test
    @DisplayName("RMSE: один великий викид збільшує RMSE більше ніж MAE")
    void rmse_outlierAffectsMoreThanMAE() {
        List<Double> actual    = Arrays.asList(10.0, 10.0, 10.0, 10.0);
        List<Double> predicted = Arrays.asList(10.0, 10.0, 10.0, 50.0); // один великий викид
        double mae  = evaluator.mae(actual, predicted);
        double rmse = evaluator.rmse(actual, predicted);
        assertTrue(rmse > mae * 1.5); // RMSE значно більший за MAE через викид
    }
}
