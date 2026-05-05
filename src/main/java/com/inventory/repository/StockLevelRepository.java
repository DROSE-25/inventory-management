package com.inventory.repository;

import com.inventory.model.StockLevel;
import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @Query("SELECT s FROM StockLevel s WHERE s.reorderPoint IS NOT NULL AND s.quantity <= s.reorderPoint")
    List<StockLevel> findBelowReorderPoint();
}