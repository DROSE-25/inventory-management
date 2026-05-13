package com.inventory.forecast;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.evaluation.ForecastAccuracyEvaluator;
import com.inventory.forecast.methods.SimpleMovingAverageMethod;
import com.inventory.forecast.model.ForecastResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
class SimpleMovingAverageMethodTest {
 
    private SimpleMovingAverageMethod sma;
 
    @BeforeEach
    void setUp() {
        sma = new SimpleMovingAverageMethod(new ForecastAccuracyEvaluator());
    }
 
    @Test
    void forecast_window3_returnsCorrectAverage() {
        // Дані: 10, 20, 30 -> прогноз = (10+20+30)/3 = 20
        List<DataPoint> history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 10),
            new DataPoint(LocalDate.of(2025,1,2), 20),
            new DataPoint(LocalDate.of(2025,1,3), 30)
        );
        ForecastResult result = sma.forecast(history, 1);
        assertEquals(1, result.getForecast().size());
        assertEquals(20.0, result.getForecast().get(0).getValue(), 0.001);
    }
 
    @Test
    void forecast_horizon3_returnsThreePoints() {
        List<DataPoint> history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 10),
            new DataPoint(LocalDate.of(2025,1,2), 20),
            new DataPoint(LocalDate.of(2025,1,3), 30)
        );
        ForecastResult result = sma.forecast(history, 3);
        assertEquals(3, result.getForecast().size());
    }
 
    @Test
    void getMethodName_returnsSMA() {
        assertEquals("SMA", sma.getMethodName());
    }
 
    @Test
    void forecast_datesAreSequential() {
        List<DataPoint> history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 10),
            new DataPoint(LocalDate.of(2025,1,2), 20),
            new DataPoint(LocalDate.of(2025,1,3), 30)
        );
        ForecastResult result = sma.forecast(history, 2);
        assertEquals(LocalDate.of(2025,1,4), result.getForecast().get(0).getDate());
        assertEquals(LocalDate.of(2025,1,5), result.getForecast().get(1).getDate());
    }
}
