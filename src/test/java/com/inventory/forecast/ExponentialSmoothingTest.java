package com.inventory.forecast;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.evaluation.ForecastAccuracyEvaluator;
import com.inventory.forecast.methods.HoltExponentialSmoothingMethod;
import com.inventory.forecast.methods.SingleExponentialSmoothingMethod;
import com.inventory.forecast.model.ForecastResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
class ExponentialSmoothingTest {
 
    private SingleExponentialSmoothingMethod ses;
    private HoltExponentialSmoothingMethod holt;
    private List<DataPoint> history;
 
    @BeforeEach
    void setUp() {
        ForecastAccuracyEvaluator eval = new ForecastAccuracyEvaluator();
        ses  = new SingleExponentialSmoothingMethod(eval);
        holt = new HoltExponentialSmoothingMethod(eval);
        history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 100),
            new DataPoint(LocalDate.of(2025,1,2), 110),
            new DataPoint(LocalDate.of(2025,1,3), 120),
            new DataPoint(LocalDate.of(2025,1,4), 130),
            new DataPoint(LocalDate.of(2025,1,5), 140),
            new DataPoint(LocalDate.of(2025,1,6), 150)
        );
    }
 
    // --- SES тести ---
    @Test
    void ses_getMethodName() { assertEquals("SES", ses.getMethodName()); }
 
    @Test
    void ses_forecast_returnsCorrectHorizon() {
        ForecastResult r = ses.forecast(history, 5);
        assertEquals(5, r.getForecast().size());
    }
 
    @Test
    void ses_forecast_stableData_allSameValue() {
        // При стабільних даних SES прогнозує одне значення для всіх горизонтів
        ForecastResult r = ses.forecast(history, 3);
        double first = r.getForecast().get(0).getValue();
        double second = r.getForecast().get(1).getValue();
        assertEquals(first, second, 0.001);
    }
 
    @Test
    void ses_tooFewPoints_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> ses.forecast(List.of(new DataPoint(LocalDate.now(), 10)), 1));
    }
 
    // --- Holt тести ---
    @Test
    void holt_getMethodName() { assertEquals("HOLT", holt.getMethodName()); }
 
    @Test
    void holt_forecast_returnsCorrectHorizon() {
        ForecastResult r = holt.forecast(history, 4);
        assertEquals(4, r.getForecast().size());
    }
 
    @Test
    void holt_forecast_trendData_valuesIncreasing() {
        // Для зростаючого ряду Holt повинен прогнозувати зростаючі значення
        ForecastResult r = holt.forecast(history, 3);
        double f1 = r.getForecast().get(0).getValue();
        double f3 = r.getForecast().get(2).getValue();
        assertTrue(f3 > f1, "Holt should forecast increasing values for trend data");
    }
 
    @Test
    void holt_tooFewPoints_throws() {
        List<DataPoint> two = List.of(
            new DataPoint(LocalDate.now(), 10),
            new DataPoint(LocalDate.now().plusDays(1), 20)
        );
        assertThrows(IllegalArgumentException.class, () -> holt.forecast(two, 1));
    }
}

