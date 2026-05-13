package com.inventory.forecast;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.evaluation.ForecastAccuracyEvaluator;
import com.inventory.forecast.methods.WeightedMovingAverageMethod;
import com.inventory.forecast.model.ForecastResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
 
class WeightedMovingAverageMethodTest {
 
    private WeightedMovingAverageMethod wma;
 
    @BeforeEach
    void setUp() {
        wma = new WeightedMovingAverageMethod(new ForecastAccuracyEvaluator());
    }
 
    @Test
    void forecast_window3_returnsWeightedAverage() {
        // Дані: 10, 20, 30
        // WMA = (1*10 + 2*20 + 3*30) / (1+2+3) = 140/6 = 23.333
        List<DataPoint> history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 10),
            new DataPoint(LocalDate.of(2025,1,2), 20),
            new DataPoint(LocalDate.of(2025,1,3), 30)
        );
        ForecastResult result = wma.forecast(history, 1);
        assertEquals(23.333, result.getForecast().get(0).getValue(), 0.001);
    }
 
    @Test
    void forecast_wmaHigherThanSma_whenTrendUp() {
        // При зростаючому тренді WMA > SMA (більша вага на нові дані)
        List<DataPoint> history = List.of(
            new DataPoint(LocalDate.of(2025,1,1), 10),
            new DataPoint(LocalDate.of(2025,1,2), 20),
            new DataPoint(LocalDate.of(2025,1,3), 30)
        );
        ForecastResult result = wma.forecast(history, 1);
        // WMA = 23.33 > SMA = 20
        assertTrue(result.getForecast().get(0).getValue() > 20.0);
    }
 
    @Test
    void getMethodName_returnsWMA() {
        assertEquals("WMA", wma.getMethodName());
    
    }}