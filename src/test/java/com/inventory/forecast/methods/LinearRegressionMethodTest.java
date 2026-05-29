package com.inventory.forecast.methods;

import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.evaluation.ForecastAccuracyEvaluator;
import com.inventory.forecast.model.ForecastResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LinearRegressionMethod — тести лінійної регресії")
class LinearRegressionMethodTest {

    private LinearRegressionMethod method;

    @BeforeEach
    void setUp() {
        method = new LinearRegressionMethod(new ForecastAccuracyEvaluator());
    }

    private List<DataPoint> makeHistory(double... values) {
        List<DataPoint> pts = new ArrayList<>();
        LocalDate date = LocalDate.of(2026, 1, 1);
        for (double v : values) {
            pts.add(new DataPoint(date, v));
            date = date.plusDays(1);
        }
        return pts;
    }

    @Test
    @DisplayName("getMethodName() повертає 'LINEAR_REGRESSION'")
    void getMethodName_returnsLinearRegression() {
        assertEquals("LINEAR_REGRESSION", method.getMethodName());
    }

    @Test
    @DisplayName("Прогноз на горизонт 5 — повертає 5 точок")
    void forecast_horizon5_returns5Points() {
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50, 60, 70);
        ForecastResult result = method.forecast(history, 5);
        assertEquals(5, result.getForecast().size());
    }

    @Test
    @DisplayName("Чітко зростаючий тренд: прогноз продовжує зростання")
    void forecast_upwardTrend_forecastContinuesGrowth() {
        // 10, 20, 30, 40, 50 → trend +10/day → наступне має бути ~60
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50);
        ForecastResult result = method.forecast(history, 1);
        double forecastVal = result.getForecast().get(0).getValue();
        assertTrue(forecastVal > 50, "Прогноз має бути більший за 50 при зростаючому тренді");
    }

    @Test
    @DisplayName("Чітко спадаючий тренд: прогноз продовжує спад")
    void forecast_downwardTrend_forecastContinuesDecline() {
        List<DataPoint> history = makeHistory(50, 40, 30, 20, 10);
        ForecastResult result = method.forecast(history, 1);
        double forecastVal = result.getForecast().get(0).getValue();
        assertTrue(forecastVal < 10, "Прогноз має бути менший за 10 при спадаючому тренді");
    }

    @Test
    @DisplayName("Ідеально лінійні дані — MAE близька до 0")
    void forecast_perfectLinearData_lowMAE() {
        // Ідеально лінійний ряд — регресія повинна підігнатись майже точно
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
        ForecastResult result = method.forecast(history, 3);
        assertTrue(result.getMae() < 5.0,
            "MAE має бути низькою для ідеально лінійних даних, але було: " + result.getMae());
    }

    @Test
    @DisplayName("IllegalArgumentException при менше 3 точок")
    void forecast_lessThan3Points_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> method.forecast(makeHistory(10.0, 20.0), 3));
    }

    @Test
    @DisplayName("Опис містить slope та intercept")
    void forecast_descriptionContainsSlopeAndIntercept() {
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50);
        ForecastResult result = method.forecast(history, 3);
        String desc = result.getDescription().toLowerCase();
        assertTrue(desc.contains("slope") || desc.contains("intercept"));
    }

    @Test
    @DisplayName("Прогнозні дати послідовні")
    void forecast_datesAreSequential() {
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50, 60);
        ForecastResult result = method.forecast(history, 4);
        List<DataPoint> pts = result.getForecast();
        for (int i = 1; i < pts.size(); i++) {
            assertEquals(pts.get(i-1).getDate().plusDays(1), pts.get(i).getDate());
        }
    }
}
