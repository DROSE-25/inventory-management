package com.inventory.forecast;

import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.methods.ForecastingMethod;
import com.inventory.forecast.methods.NaiveForecastMethod;
import com.inventory.forecast.model.ForecastResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    private ForecastService forecastService;
    private List<DataPoint> history;
    private ForecastingMethod naiveMethod;

    @BeforeEach
    void setUp() {
        // Готуємо дані для тестів: сервіс та список з 3-х точок історії
        naiveMethod = new NaiveForecastMethod();
        forecastService = new ForecastService(List.of(naiveMethod));
        
        history = List.of(
            new DataPoint(LocalDate.now().minusDays(2), 100.0),
            new DataPoint(LocalDate.now().minusDays(1), 110.0),
            new DataPoint(LocalDate.now(), 120.0)
        );
    }

    @Test
    void runForecast_unknownMethod_throwsException() {
        // Перевірка, що "UNKNOWN" метод викличе помилку IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
            forecastService.runForecast("UNKNOWN", history, 5));
    }

    @Test
    void runForecast_naiveMethod_returnsCorrectSize() {
        // Перевірка, що запит на 3 дні (horizon=3) повертає рівно 3 точки
        ForecastResult result = forecastService.runForecast(naiveMethod.getMethodName(), history, 3);
        assertEquals(3, result.getForecast().size());
    }
}