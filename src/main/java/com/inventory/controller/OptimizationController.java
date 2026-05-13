package com.inventory.controller;

import com.inventory.repository.ProductRepository;
import com.inventory.optimization.OptimizationService;
import com.inventory.optimization.dto.OptimizationRecommendation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/optimization")
@RequiredArgsConstructor
@Tag(name = "Optimization", description = "EOQ, Safety Stock, Reorder Point")
public class OptimizationController {

    private final OptimizationService optimizationService;
    private final ProductRepository   productRepository;

    @Operation(summary = "Рекомендація по запасах для конкретного товару")
    @PostMapping("/recommendations/{productId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<OptimizationRecommendation> getRecommendation(
            @PathVariable Long productId) {
        return ResponseEntity.ok(
            optimizationService.getRecommendation(productId));
    }

    @Operation(summary = "Список всіх товарів що потребують перезамовлення")
    @GetMapping("/reorder-alerts")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<OptimizationRecommendation>> getReorderAlerts() {
        List<OptimizationRecommendation> alerts = productRepository.findAll().stream()
            .map(p -> optimizationService.getRecommendation(p.getId()))
            .filter(OptimizationRecommendation::isNeedsReorder)
            .collect(Collectors.toList());
        return ResponseEntity.ok(alerts);
    }
}
