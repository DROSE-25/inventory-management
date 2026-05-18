package com.inventory.controller;

import com.inventory.service.ExcelExportService;
import com.inventory.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Експорт звітів")
public class ReportExportController {

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    @Operation(summary = "Завантажити ABC/XYZ аналіз у Excel")
    @GetMapping("/excel/abc-xyz")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','MANAGER')")
    public ResponseEntity<byte[]> downloadAbcXyzExcel() throws Exception {
        byte[] data = excelExportService.exportAbcXyz();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=abc_xyz_analysis.xlsx")
            .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(data);
    }

    @Operation(summary = "Завантажити ABC/XYZ аналіз у PDF")
    @GetMapping("/pdf/abc-xyz")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','MANAGER')")
    public ResponseEntity<byte[]> downloadAbcXyzPdf() throws Exception {
        byte[] data = pdfExportService.exportAbcXyzPdf();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=abc_xyz_analysis.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(data);
    }
}