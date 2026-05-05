package com.inventory.repository;
 
import com.inventory.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByIsActiveTrue();
}
