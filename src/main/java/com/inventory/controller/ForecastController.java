package com.inventory.controller;

import com.inventory.forecast.ForecastService;
import com.inventory.forecast.dto.CompareResponse;
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.model.ForecastResult;
import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
@Tag(name = "Forecast", description = "Прогнозування попиту")
public class ForecastController {

    private final ForecastService forecastService;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;

    @Operation(summary = "Список доступних методів")
    @GetMapping("/methods")
    @PreAuthorize("hasAnyRole('ANALYST','MANAGER','ADMIN')")
    public ResponseEntity<List<String>> getMethods() {
        return ResponseEntity.ok(forecastService.getAvailableMethods());
    }

    @Operation(summary = "Запустити ручний прогноз по переданій історії")
    @PostMapping("/run")
    @PreAuthorize("hasAnyRole('ANALYST','MANAGER','ADMIN')")
    public ResponseEntity<ForecastResult> runForecast(
            @RequestParam String method,
            @RequestParam(defaultValue = "7") int horizon,
            @RequestBody List<DataPoint> history) {
        return ResponseEntity.ok(forecastService.runForecast(method, history, horizon));
    }

    @Operation(summary = "Порівняти всі методи для товару")
    @PostMapping("/compare/{productId}")
    @PreAuthorize("hasAnyRole('ANALYST','MANAGER','ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<CompareResponse> compare(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") int horizon) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(90);
        
        List<DataPoint> history = saleRepository
                .findByProductIdAndSaleDateBetweenOrderBySaleDateAsc(productId, from, to)
                .stream()
                .map(s -> new DataPoint(s.getSaleDate(), s.getQuantity().doubleValue()))
                .collect(Collectors.toList());

        if (history.size() < 2) {
            return ResponseEntity.badRequest().build();
        }

        List<ForecastResult> results = forecastService.getAvailableMethods().stream()
                .map(name -> {
                    try {
                        return forecastService.runForecast(name, history, horizon);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(r -> r != null && !Double.isNaN(r.getMape()))
                .collect(Collectors.toList());

        ForecastResult best = results.stream()
                .min(Comparator.comparingDouble(ForecastResult::getMape))
                .orElse(null);

        return ResponseEntity.ok(CompareResponse.builder()
                .productId(productId)
                .productName(product.getName())
                .horizon(horizon)
                .results(results)
                .best(best)
                .build());
    }

    @Operation(summary = "Найкращий метод для товару")
    @GetMapping("/best/{productId}")
    @PreAuthorize("hasAnyRole('ANALYST','MANAGER','ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<ForecastResult> getBest(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") int horizon) {

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(90);
        
        List<DataPoint> history = saleRepository
                .findByProductIdAndSaleDateBetweenOrderBySaleDateAsc(productId, from, to)
                .stream()
                .map(s -> new DataPoint(s.getSaleDate(), s.getQuantity().doubleValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(forecastService.getBestForecast(history, horizon));
    }
}