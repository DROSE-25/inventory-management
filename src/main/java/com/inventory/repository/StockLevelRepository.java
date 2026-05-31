package com.inventory.repository;

import com.inventory.model.StockLevel;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("SELECT s FROM StockLevel s JOIN FETCH s.warehouse WHERE s.product.id = :productId")
    List<StockLevel> findByProductIdWithWarehouse(@Param("productId") Long productId);

    @Query("""
        SELECT s FROM StockLevel s
        WHERE s.reorderPoint IS NOT NULL
          AND s.quantity <= s.reorderPoint
          AND s.product.companyId = :companyId
        """)
    List<StockLevel> findBelowReorderPointByCompany(@Param("companyId") Long companyId);

    // Залишаємо старий для сумісності
    @Query("SELECT s FROM StockLevel s WHERE s.reorderPoint IS NOT NULL AND s.quantity <= s.reorderPoint")
    List<StockLevel> findBelowReorderPoint();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockLevel s WHERE s.product.id = :productId AND s.warehouse.id = :warehouseId")
    Optional<StockLevel> findByProductIdAndWarehouseIdForUpdate(
        @Param("productId") Long productId,
        @Param("warehouseId") Long warehouseId);
}