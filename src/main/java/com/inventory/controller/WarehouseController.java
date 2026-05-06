package com.inventory.controller;
 
import com.inventory.dto.request.WarehouseRequest;
import com.inventory.dto.response.WarehouseResponse;
import com.inventory.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
 
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Управління складами")
public class WarehouseController {
 
    private final WarehouseService warehouseService;
 
    @GetMapping
    @Operation(summary = "Список всіх складів")
    public List<WarehouseResponse> findAll() {
        return warehouseService.findAll();
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Отримати склад за ID")
    public WarehouseResponse findById(@PathVariable Long id) {
        return warehouseService.findById(id);
    }
 
    @PostMapping
    @Operation(summary = "Додати склад")
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.create(request));
    }
}
