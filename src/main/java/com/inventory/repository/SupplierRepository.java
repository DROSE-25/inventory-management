package com.inventory.repository;
 
import com.inventory.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
 
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByName(String name);
    List<Supplier> findByIsActiveTrue();
}
