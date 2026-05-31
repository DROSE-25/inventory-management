package com.inventory.repository;

import com.inventory.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByIsActiveTrueAndCompanyId(Long companyId);

    Optional<Warehouse> findByNameAndCompanyId(String name, Long companyId);

    List<Warehouse> findByCompanyId(Long companyId);
}
