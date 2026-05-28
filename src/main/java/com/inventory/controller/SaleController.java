package com.inventory.controller;

import com.inventory.dto.request.SaleRequest;
import com.inventory.dto.response.SaleResponse;
import com.inventory.dto.response.SaleImportResult;
import com.inventory.dto.SalesAggregationPoint;
import com.inventory.service.SaleService;
import com.inventory.service.SalesAggregationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Реєстрація та імпорт продажів")
public class SaleController {

    private final SaleService saleService;
    private final SalesAggregationService aggregationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
    public List<SaleResponse> getAll(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        if (from != null && to != null) {
            return saleService.getByDateRange(from, to);
        }
        return saleService.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public SaleResponse register(@Valid @RequestBody SaleRequest request) {
        return saleService.registerSale(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public SaleImportResult importFile(@RequestParam("file") MultipartFile file)
            throws IOException {
        String name = file.getOriginalFilename();
        if (name == null) throw new IllegalArgumentException("Файл без імені");

        if (name.endsWith(".csv")) {
            return saleService.importFromCsv(file.getInputStream());
        } else if (name.endsWith(".xlsx")) {
            return saleService.importFromExcel(file.getInputStream());
        } else {
            throw new IllegalArgumentException(
                "Підтримуються лише .csv та .xlsx файли");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void delete(@PathVariable Long id) {
        saleService.deleteSale(id);
    }

    @GetMapping("/aggregate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ANALYST')")
    public List<SalesAggregationPoint> aggregate(
            @RequestParam Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(defaultValue = "month") String granularity) {
        return aggregationService.aggregate(productId, warehouseId, from, to, granularity);
    }
}