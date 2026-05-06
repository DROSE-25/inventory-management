package com.inventory.controller;
 
import com.inventory.dto.request.ProductRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
 
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Управління товарами")
public class ProductController {
 
    private final ProductService productService;
 
    @GetMapping
    @Operation(summary = "Список всіх активних товарів з пагінацією")
    public Page<ProductResponse> findAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return productService.findAll(pageable);
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Отримати товар за ID")
    public ProductResponse findById(@PathVariable Long id) {
        return productService.findById(id);
    }
 
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Отримати товар за SKU")
    public ProductResponse findBySku(@PathVariable String sku) {
        return productService.findBySku(sku);
    }
 
    @GetMapping("/search")
    @Operation(summary = "Пошук товарів за назвою")
    public List<ProductResponse> search(@RequestParam String name) {
        return productService.search(name);
    }
 
    @PostMapping
    @Operation(summary = "Створити новий товар")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }
 
    @PutMapping("/{id}")
    @Operation(summary = "Оновити товар")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }
 
    @DeleteMapping("/{id}")
    @Operation(summary = "М'яке видалення товару")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}

