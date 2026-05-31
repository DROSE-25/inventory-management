package com.inventory.repository;

import com.inventory.model.AbcXyzResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbcXyzResultRepository extends JpaRepository<AbcXyzResult, Long> {

    List<AbcXyzResult> findAllByCompanyIdOrderByRevenueDesc(Long companyId);

    List<AbcXyzResult> findByAbcClassAndCompanyId(String abcClass, Long companyId);

    List<AbcXyzResult> findByXyzClassAndCompanyId(String xyzClass, Long companyId);

    List<AbcXyzResult> findByCombinedClassAndCompanyId(String combinedClass, Long companyId);

    Optional<AbcXyzResult> findByProductIdAndCompanyId(Long productId, Long companyId);

    @Modifying
    @Query("DELETE FROM AbcXyzResult r WHERE r.periodFrom = :from AND r.periodTo = :to " +
           "AND r.product.companyId = :companyId")
    void deleteByPeriodAndCompanyId(@Param("from") LocalDate from,
                                    @Param("to") LocalDate to,
                                    @Param("companyId") Long companyId);
}
