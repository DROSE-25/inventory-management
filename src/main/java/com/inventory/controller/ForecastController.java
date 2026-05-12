package com.inventory.controller;
 
import com.inventory.forecast.ForecastService;
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.model.ForecastResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
 
@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
public class ForecastController {
 
    private final ForecastService forecastService;
 
    // Отримати список доступних методів
    @GetMapping("/methods")
    public List<String> getMethods() {
        return forecastService.getAvailableMethods();
    }
 
    // Запустити прогноз
    @PostMapping("/run")
    public ResponseEntity<ForecastResult> runForecast(
            @RequestParam String method,
            @RequestParam(defaultValue = "7") int horizon,
            @RequestBody List<DataPoint> history) {
        return ResponseEntity.ok(forecastService.runForecast(method, history, horizon));
    }
}
