package com.inventory.controller;

import com.inventory.dto.request.SupplierRequest;
import com.inventory.dto.response.SupplierResponse;
import com.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Управління постачальниками")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Список всіх постачальників")
    public List<SupplierResponse> findAll() {
        return supplierService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати постачальника за ID")
    public SupplierResponse findById(@PathVariable Long id) {
        return supplierService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Додати постачальника")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Деактивувати постачальника")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        supplierService.delete(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити постачальника")
    public SupplierResponse update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(id, request);
    }
}