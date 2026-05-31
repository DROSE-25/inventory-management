package com.inventory.controller;

import com.inventory.model.Category;
import com.inventory.repository.CategoryRepository;
import com.inventory.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Управління категоріями")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final SecurityUtils      securityUtils;

    @Operation(summary = "Список категорій компанії")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        Long companyId = securityUtils.getCurrentCompanyId();
        List<Map<String, Object>> result = categoryRepository.findByCompanyId(companyId)
            .stream()
            .map(c -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", c.getId());
                map.put("name", c.getName());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Створити категорію")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> request) {
        Long companyId = securityUtils.getCurrentCompanyId();
        Category category = new Category();
        category.setName(request.get("name"));
        category.setCompanyId(companyId);
        Category saved = categoryRepository.save(category);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", saved.getId());
        map.put("name", saved.getName());
        return ResponseEntity.ok(map);
    }

    @Operation(summary = "Видалити категорію")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long companyId = securityUtils.getCurrentCompanyId();
        categoryRepository.findById(id)
            .filter(c -> c.getCompanyId().equals(companyId))
            .ifPresent(categoryRepository::delete);
        return ResponseEntity.noContent().build();
    }
}
