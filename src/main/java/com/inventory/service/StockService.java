package com.inventory.service;
 
import com.inventory.dto.response.StockLevelResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.exception.ValidationException;
import com.inventory.model.StockLevel;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
 
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {
 
    private final StockLevelRepository stockLevelRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
 
    public List<StockLevelResponse> findBelowReorderPoint() {
        return stockLevelRepository.findBelowReorderPoint()
            .stream().map(this::toResponse).toList();
    }
 
    public StockLevelResponse findByProductAndWarehouse(Long productId, Long warehouseId) {
        return stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("StockLevel не знайдено"));
    }
 
    // Надходження товару на склад
    @Transactional
    public StockLevelResponse receiveStock(Long productId, Long warehouseId, BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Кількість повинна бути більше 0");
        }
        StockLevel stock = getOrCreate(productId, warehouseId);
        stock.setQuantity(stock.getQuantity().add(quantity));
        stock.setUpdatedAt(OffsetDateTime.now());
        return toResponse(stockLevelRepository.save(stock));
    }
 
    // Списання товару зі складу
    @Transactional
    public StockLevelResponse deductStock(Long productId, Long warehouseId, BigDecimal quantity) {
        StockLevel stock = getOrCreate(productId, warehouseId);
        if (stock.getQuantity().compareTo(quantity) < 0) {
            throw new ValidationException("Недостатньо товару на складі");
        }
        stock.setQuantity(stock.getQuantity().subtract(quantity));
        stock.setUpdatedAt(OffsetDateTime.now());
        return toResponse(stockLevelRepository.save(stock));
    }
 
    private StockLevel getOrCreate(Long productId, Long warehouseId) {
        return stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseGet(() -> {
                StockLevel s = new StockLevel();
                s.setProduct(productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId)));
                s.setWarehouse(warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId)));
                s.setQuantity(BigDecimal.ZERO);
                s.setUpdatedAt(OffsetDateTime.now());
                return s;
            });
    }
 
    private StockLevelResponse toResponse(StockLevel s) {
        boolean below = s.getReorderPoint() != null &&
            s.getQuantity().compareTo(s.getReorderPoint()) <= 0;
        return StockLevelResponse.builder()
            .id(s.getId())
            .productId(s.getProduct().getId())
            .productName(s.getProduct().getName())
            .productSku(s.getProduct().getSku())
            .warehouseId(s.getWarehouse().getId())
            .warehouseName(s.getWarehouse().getName())
            .quantity(s.getQuantity())
            .reorderPoint(s.getReorderPoint())
            .safetyStock(s.getSafetyStock())
            .eoq(s.getEoq())
            .belowReorderPoint(below)
            .build();
    }
}

