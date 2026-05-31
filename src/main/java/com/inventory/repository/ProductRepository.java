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

    Optional<Product> findBySkuAndCompanyId(String sku, Long companyId);

    Page<Product> findByIsActiveTrueAndCompanyId(Long companyId, Pageable pageable);

    List<Product> findByCategoryIdAndIsActiveTrueAndCompanyId(Long categoryId, Long companyId);

    List<Product> findBySupplierIdAndIsActiveTrueAndCompanyId(Long supplierId, Long companyId);

    List<Product> findByCompanyId(Long companyId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%',:name,'%')) " +
           "AND p.isActive = true AND p.companyId = :companyId")
    List<Product> searchByNameAndCompanyId(@Param("name") String name,
                                           @Param("companyId") Long companyId);
}
