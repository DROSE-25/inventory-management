package com.inventory.forecast;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.evaluation.ForecastAccuracyEvaluator;
import com.inventory.forecast.methods.LinearRegressionMethod;
import com.inventory.forecast.model.ForecastResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
class LinearRegressionMethodTest {
 
    private LinearRegressionMethod lr;
 
    @BeforeEach
    void setUp() {
        lr = new LinearRegressionMethod(new ForecastAccuracyEvaluator());
    }
 
    @Test
    void getMethodName_returnsLinearRegression() {
        assertEquals("LINEAR_REGRESSION", lr.getMethodName());
    }
 
    @Test
    void forecast_perfectLinearData_highAccuracy() {
        // Ряд: 10, 20, 30, 40, 50 — ідеальна пряма, регресія має точно її відновити
        List<DataPoint> history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 10),
            new DataPoint(LocalDate.of(2025,1,2), 20),
            new DataPoint(LocalDate.of(2025,1,3), 30),
            new DataPoint(LocalDate.of(2025,1,4), 40),
            new DataPoint(LocalDate.of(2025,1,5), 50)
        );
        ForecastResult r = lr.forecast(history, 1);
        // Прогноз на крок 6: y = 10*t, при t=6 → 60
        assertEquals(60.0, r.getForecast().get(0).getValue(), 1.0);
    }
 
    @Test
    void forecast_horizon3_returns3Points() {
        List<DataPoint> history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 5),
            new DataPoint(LocalDate.of(2025,1,2), 10),
            new DataPoint(LocalDate.of(2025,1,3), 15),
            new DataPoint(LocalDate.of(2025,1,4), 20),
            new DataPoint(LocalDate.of(2025,1,5), 25)
        );
        assertEquals(3, lr.forecast(history, 3).getForecast().size());
    }
 
    @Test
    void forecast_tooFewPoints_throws() {
        List<DataPoint> two = List.of(
            new DataPoint(LocalDate.now(), 5),
            new DataPoint(LocalDate.now().plusDays(1), 10)
        );
        assertThrows(IllegalArgumentException.class, () -> lr.forecast(two, 1));
    }
}
