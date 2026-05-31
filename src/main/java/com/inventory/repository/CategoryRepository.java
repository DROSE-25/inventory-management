package com.inventory.repository;

import com.inventory.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndCompanyId(String name, Long companyId);

    List<Category> findByParentIsNullAndCompanyId(Long companyId);

    List<Category> findByCompanyId(Long companyId);
}
