package com.inventory.repository;

import com.inventory.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // --- Методы из первого фрагмента ---

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDate from, LocalDate to);

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


    // --- Методы из второго фрагмента ---

    Page<Sale> findByProductId(Long productId, Pageable pageable);

    @Query(value = """
        SELECT date_trunc(:granularity, sale_date) AS period,
               SUM(quantity)                  AS total_qty,
               SUM(quantity * unit_price)     AS total_revenue
        FROM sales
        WHERE product_id = :productId
          AND (:warehouseId IS NULL OR warehouse_id = :warehouseId)
          AND sale_date BETWEEN :fromDate AND :toDate
        GROUP BY 1
        ORDER BY 1
        """, nativeQuery = true)
    List<Object[]> aggregateByPeriod(
        @Param("granularity") String granularity,  // 'day' | 'week' | 'month'
        @Param("productId")   Long productId,
        @Param("warehouseId") Long warehouseId,    // может быть null
        @Param("fromDate")    LocalDate fromDate,
        @Param("toDate")      LocalDate toDate);
}