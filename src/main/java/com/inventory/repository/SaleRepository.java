package com.inventory.repository;

import com.inventory.model.Sale;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByProductIdAndSaleDateBetweenOrderBySaleDateAsc(
        Long productId, LocalDate from, LocalDate to);

    @Query("""
        SELECT FUNCTION('date_trunc','month', s.saleDate) as month,
               SUM(s.quantity) as totalQty
        FROM Sale s WHERE s.product.id = :productId
        GROUP BY FUNCTION('date_trunc','month', s.saleDate)
        ORDER BY 1
        """)
    List<Object[]> findMonthlyDemand(@Param("productId") Long productId);
}