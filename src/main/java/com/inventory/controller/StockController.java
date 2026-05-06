package com.inventory.controller;
 
import com.inventory.dto.response.StockLevelResponse;
import com.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
 
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Управління залишками")
public class StockController {
 
    private final StockService stockService;
 
    @GetMapping("/below-reorder-point")
    @Operation(summary = "Товари нижче точки перезамовлення")
    public List<StockLevelResponse> findBelowReorderPoint() {
        return stockService.findBelowReorderPoint();
    }
 
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @Operation(summary = "Залишок конкретного товару на складі")
    public StockLevelResponse findStock(@PathVariable Long productId,
                                         @PathVariable Long warehouseId) {
        return stockService.findByProductAndWarehouse(productId, warehouseId);
    }
 
    @PostMapping("/product/{productId}/warehouse/{warehouseId}/receive")
    @Operation(summary = "Надходження товару на склад")
    public StockLevelResponse receive(@PathVariable Long productId,
                                       @PathVariable Long warehouseId,
                                       @RequestParam BigDecimal quantity) {
        return stockService.receiveStock(productId, warehouseId, quantity);
    }
 
    @PostMapping("/product/{productId}/warehouse/{warehouseId}/deduct")
    @Operation(summary = "Списання товару зі складу")
    public StockLevelResponse deduct(@PathVariable Long productId,
                                      @PathVariable Long warehouseId,
                                      @RequestParam BigDecimal quantity) {
        return stockService.deductStock(productId, warehouseId, quantity);
    }
}

