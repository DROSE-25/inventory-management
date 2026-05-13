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

    List<AbcXyzResult> findAllByOrderByRevenueDesc();

    List<AbcXyzResult> findByAbcClass(String abcClass);

    List<AbcXyzResult> findByXyzClass(String xyzClass);

    List<AbcXyzResult> findByCombinedClass(String combinedClass);

    Optional<AbcXyzResult> findByProductId(Long productId);

    // Видалити старі результати перед перерахунком
    @Modifying
    @Query("DELETE FROM AbcXyzResult r WHERE r.periodFrom = :from AND r.periodTo = :to")
    void deleteByPeriod(@Param("from") LocalDate from, @Param("to") LocalDate to);
}