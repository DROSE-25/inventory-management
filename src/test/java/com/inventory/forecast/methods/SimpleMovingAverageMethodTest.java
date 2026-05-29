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

@DisplayName("SimpleMovingAverageMethod — тести SMA прогнозування")
class SimpleMovingAverageMethodTest {

    private SimpleMovingAverageMethod method;

    @BeforeEach
    void setUp() {
        method = new SimpleMovingAverageMethod(new ForecastAccuracyEvaluator());
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
    @DisplayName("getMethodName() повертає 'SMA'")
    void getMethodName_returnsSMA() {
        assertEquals("SMA", method.getMethodName());
    }

    @Test
    @DisplayName("Прогноз на горизонт 3 — повертає 3 точки")
    void forecast_horizon3_returns3Points() {
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50);
        ForecastResult result = method.forecast(history, 3);
        assertEquals(3, result.getForecast().size());
    }

    @Test
    @DisplayName("SMA(3) для [10,20,30,40,50]: перший прогноз = (30+40+50)/3 = 40")
    void forecast_sma3_firstForecastIsAvgOfLast3() {
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50);
        ForecastResult result = method.forecast(history, 1);
        assertEquals(40.0, result.getForecast().get(0).getValue(), 0.001);
    }

    @Test
    @DisplayName("Стабільний попит: прогноз = середньому значенню")
    void forecast_stableDemand_forecastEqualsAverage() {
        List<DataPoint> history = makeHistory(10, 10, 10, 10, 10);
        ForecastResult result = method.forecast(history, 5);
        for (DataPoint dp : result.getForecast()) {
            assertEquals(10.0, dp.getValue(), 0.001);
        }
    }

    @Test
    @DisplayName("MAE для ідеального прогнозу = 0")
    void forecast_perfectHistory_maeIsZero() {
        List<DataPoint> history = makeHistory(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        ForecastResult result = method.forecast(history, 3);
        assertEquals(0.0, result.getMae(), 0.001);
        assertEquals(0.0, result.getMape(), 0.001);
    }

    @Test
    @DisplayName("Прогнозні дати йдуть послідовно по одному дню")
    void forecast_datesAreSequential() {
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50);
        ForecastResult result = method.forecast(history, 3);
        List<DataPoint> pts = result.getForecast();
        for (int i = 1; i < pts.size(); i++) {
            assertEquals(pts.get(i-1).getDate().plusDays(1), pts.get(i).getDate());
        }
    }

    @Test
    @DisplayName("Метод повертає ненульові MAE/RMSE при нестабільному попиті")
    void forecast_unstableDemand_hasPositiveErrors() {
        List<DataPoint> history = makeHistory(10, 50, 5, 80, 3, 70, 8, 60, 4, 75);
        ForecastResult result = method.forecast(history, 3);
        assertTrue(result.getMae() > 0);
        assertTrue(result.getRmse() > 0);
    }

    @Test
    @DisplayName("Опис містить 'window'")
    void forecast_descriptionContainsWindow() {
        List<DataPoint> history = makeHistory(10, 20, 30, 40, 50);
        ForecastResult result = method.forecast(history, 3);
        assertTrue(result.getDescription().toLowerCase().contains("window"));
    }
}
