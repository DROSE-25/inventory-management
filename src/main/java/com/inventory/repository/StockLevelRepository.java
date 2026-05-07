package com.inventory.repository;

import com.inventory.model.StockLevel;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType; // Проверь этот импорт

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("SELECT s FROM StockLevel s WHERE s.reorderPoint IS NOT NULL AND s.quantity <= s.reorderPoint")
    List<StockLevel> findBelowReorderPoint();

    @Lock(LockModeType.PESSIMISTIC_WRITE) // Добавили @ и скобки
    @Query("SELECT s FROM StockLevel s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId")
    Optional<StockLevel> findByProductIdAndWarehouseIdForUpdate(
        @Param("productId") Long productId, 
        @Param("warehouseId") Long warehouseId);
}