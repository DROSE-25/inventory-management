package com.inventory.repository;

import com.inventory.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>,
                                            JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);
    Page<Product> findByIsActiveTrue(Pageable pageable);
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);
    List<Product> findBySupplierIdAndIsActiveTrue(Long supplierId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')) AND p.isActive = true")
    List<Product> searchByName(@Param("name") String name);
}