package com.inventory.service;

import com.inventory.dto.response.StockLevelResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.exception.ValidationException;
import com.inventory.model.StockLevel;
import com.inventory.repository.*;
import com.inventory.security.SecurityUtils;
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
    private final ProductRepository    productRepository;
    private final WarehouseRepository  warehouseRepository;
    private final SecurityUtils        securityUtils;

    public List<StockLevelResponse> findBelowReorderPoint() {
        Long companyId = securityUtils.getCurrentCompanyId();
        return stockLevelRepository.findBelowReorderPointByCompany(companyId)
            .stream().map(this::toResponse).toList();
    }

    public List<StockLevelResponse> findByWarehouse(Long warehouseId) {
        Long companyId = securityUtils.getCurrentCompanyId();
        return stockLevelRepository.findAll()
            .stream()
            .filter(s -> s.getWarehouse().getId().equals(warehouseId))
            .filter(s -> s.getProduct().getCompanyId().equals(companyId))
            .filter(s -> s.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .sorted((a, b) -> b.getQuantity().compareTo(a.getQuantity()))
            .map(this::toResponse)
            .toList();
    }

    /** Всі склади де є цей товар (quantity > 0) */
    public List<StockLevelResponse> findByProduct(Long productId) {
        return stockLevelRepository.findByProductIdWithWarehouse(productId)
            .stream()
            .filter(s -> s.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .map(this::toResponse)
            .toList();
    }

    public StockLevelResponse findByProductAndWarehouse(Long productId, Long warehouseId) {
        return stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("StockLevel не знайдено"));
    }

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