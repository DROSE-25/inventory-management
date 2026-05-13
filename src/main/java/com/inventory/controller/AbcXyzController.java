package com.inventory.controller;

import com.inventory.analysis.AbcXyzService;
import com.inventory.analysis.dto.AbcXyzResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "ABC/XYZ Analysis", description = "Аналіз асортименту за оборотом та стабільністю попиту")
public class AbcXyzController {

    private final AbcXyzService abcXyzService;

    @Operation(summary = "ABC-аналіз всіх товарів")
    @GetMapping("/abc")
    @PreAuthorize("hasAnyRole('MANAGER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<AbcXyzResponse>> getAbc() {
        return ResponseEntity.ok(abcXyzService.getByAbcClass("A"));
        // якщо потрібні всі — замінити на abcXyzService.getAll()
    }

    @Operation(summary = "XYZ-аналіз всіх товарів")
    @GetMapping("/xyz")
    @PreAuthorize("hasAnyRole('MANAGER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<AbcXyzResponse>> getXyz() {
        return ResponseEntity.ok(abcXyzService.getByXyzClass("X"));
    }

    @Operation(summary = "Повна ABC-XYZ матриця для всіх товарів")
    @GetMapping("/abc-xyz")
    @PreAuthorize("hasAnyRole('MANAGER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<List<AbcXyzResponse>> getAbcXyz() {
        return ResponseEntity.ok(abcXyzService.getAll());
    }

    @Operation(summary = "Перерахувати аналіз (останні 12 місяців)")
    @PostMapping("/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AbcXyzResponse>> recalculate() {
        return ResponseEntity.ok(abcXyzService.recalculate());
    }
}