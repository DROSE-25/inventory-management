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

@DisplayName("SingleExponentialSmoothingMethod — тести SES")
class SingleExponentialSmoothingMethodTest {

    private SingleExponentialSmoothingMethod method;

    @BeforeEach
    void setUp() {
        method = new SingleExponentialSmoothingMethod(new ForecastAccuracyEvaluator());
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
    @DisplayName("getMethodName() повертає 'SES'")
    void getMethodName_returnsSES() {
        assertEquals("SES", method.getMethodName());
    }

    @Test
    @DisplayName("Прогноз на горизонт 7 — повертає 7 точок")
    void forecast_horizon7_returns7Points() {
        List<DataPoint> history = makeHistory(10, 12, 9, 11, 13, 10, 12, 11, 10, 14);
        ForecastResult result = method.forecast(history, 7);
        assertEquals(7, result.getForecast().size());
    }

    @Test
    @DisplayName("SES: всі прогнозні значення однакові (плоский прогноз)")
    void forecast_allForecastValuesEqual() {
        List<DataPoint> history = makeHistory(10, 12, 9, 11, 13, 10, 12, 11, 10, 14);
        ForecastResult result = method.forecast(history, 5);
        List<DataPoint> pts = result.getForecast();
        double first = pts.get(0).getValue();
        for (DataPoint dp : pts) {
            assertEquals(first, dp.getValue(), 0.001);
        }
    }

    @Test
    @DisplayName("Стабільний попит: прогноз близький до середнього")
    void forecast_stableDemand_forecastNearAverage() {
        List<DataPoint> history = makeHistory(10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
        ForecastResult result = method.forecast(history, 3);
        assertEquals(10.0, result.getForecast().get(0).getValue(), 0.001);
    }

    @Test
    @DisplayName("IllegalArgumentException при менше 2 точок")
    void forecast_lessThan2Points_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> method.forecast(makeHistory(10.0), 3));
    }

    @Test
    @DisplayName("Опис містить alpha")
    void forecast_descriptionContainsAlpha() {
        List<DataPoint> history = makeHistory(10, 12, 9, 11, 13, 10, 12, 11);
        ForecastResult result = method.forecast(history, 3);
        assertTrue(result.getDescription().toLowerCase().contains("alpha"));
    }

    @Test
    @DisplayName("MAE невід'ємна")
    void forecast_maeIsNonNegative() {
        List<DataPoint> history = makeHistory(10, 15, 8, 20, 12, 18, 9, 14, 11, 16);
        ForecastResult result = method.forecast(history, 5);
        assertTrue(result.getMae() >= 0);
    }

    @Test
    @DisplayName("MAPE невід'ємна")
    void forecast_mapeIsNonNegative() {
        List<DataPoint> history = makeHistory(10, 15, 8, 20, 12, 18, 9, 14, 11, 16);
        ForecastResult result = method.forecast(history, 5);
        assertTrue(result.getMape() >= 0);
    }

    @Test
    @DisplayName("Прогнозні дати послідовні")
    void forecast_datesAreSequential() {
        List<DataPoint> history = makeHistory(10, 12, 9, 11, 13, 10, 12, 11);
        ForecastResult result = method.forecast(history, 4);
        List<DataPoint> pts = result.getForecast();
        for (int i = 1; i < pts.size(); i++) {
            assertEquals(pts.get(i-1).getDate().plusDays(1), pts.get(i).getDate());
        }
    }
}
