package com.inventory.controller;

import com.inventory.optimization.OptimizationService;
import com.inventory.optimization.dto.OptimizationRecommendation;
import com.inventory.repository.ProductRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/forecast-export")
@RequiredArgsConstructor
@Tag(name = "Forecast Export", description = "Експорт прогнозів")
public class ForecastExportController {

    private final OptimizationService optimizationService;
    private final ProductRepository productRepository;

    @Operation(summary = "Завантажити критичні залишки у Excel")
    @GetMapping("/excel/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','MANAGER')")
    public ResponseEntity<byte[]> downloadReorderExcel() throws Exception {
        List<OptimizationRecommendation> data = productRepository.findAll().stream()
            .map(p -> optimizationService.getRecommendation(p.getId()))
            .filter(OptimizationRecommendation::isNeedsReorder)
            .collect(java.util.stream.Collectors.toList());

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Критичні залишки");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Alt row style
            CellStyle altStyle = wb.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Звіт: Критичні залишки та рекомендації EOQ");
            CellStyle titleStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Дата звіту: " + LocalDate.now());
            dateRow.createCell(4).setCellValue("Всього позицій: " + data.size());
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            // Empty row
            sheet.createRow(2);

            // Headers
            String[] cols = {"Товар", "SKU", "Склад", "Залишок", "ROP", "EOQ", "Страх. запас", "Рекомендація"};
            Row header = sheet.createRow(3);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, i == 7 ? 15000 : 4500);
            }

            // Data rows
            int rowNum = 4;
            for (OptimizationRecommendation r : data) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? altStyle : null;

                String[] vals = {
                    r.getProductName(), r.getSku(),
                    r.getWarehouseName() != null ? r.getWarehouseName() : "—",
                    String.valueOf(r.getCurrentStock()),
                    String.valueOf(r.getReorderPoint()),
                    String.valueOf(r.getEoq()),
                    String.valueOf(r.getSafetyStock()),
                    r.getRecommendation()
                };
                for (int i = 0; i < vals.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(vals[i]);
                    if (style != null) cell.setCellStyle(style);
                }
                rowNum++;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reorder_alerts.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
        }
    }

    @Operation(summary = "Завантажити критичні залишки у PDF")
    @GetMapping("/pdf/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','MANAGER')")
    public ResponseEntity<byte[]> downloadReorderPdf() throws Exception {
        List<OptimizationRecommendation> data = productRepository.findAll().stream()
            .map(p -> optimizationService.getRecommendation(p.getId()))
            .filter(OptimizationRecommendation::isNeedsReorder)
            .collect(java.util.stream.Collectors.toList());

        Document doc = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Load Ubuntu font from classpath (supports Cyrillic)
        java.io.InputStream isReg  = getClass().getResourceAsStream("/static/Ubuntu-Regular.ttf");
        java.io.InputStream isBold = getClass().getResourceAsStream("/static/Ubuntu-Bold.ttf");
        com.itextpdf.text.pdf.BaseFont bf, bfBold;
        if (isReg != null && isBold != null) {
            byte[] reg  = isReg.readAllBytes();
            byte[] bold = isBold.readAllBytes();
            bf     = com.itextpdf.text.pdf.BaseFont.createFont("/static/Ubuntu-Regular.ttf", com.itextpdf.text.pdf.BaseFont.IDENTITY_H, true, true, reg, null);
            bfBold = com.itextpdf.text.pdf.BaseFont.createFont("/static/Ubuntu-Bold.ttf",    com.itextpdf.text.pdf.BaseFont.IDENTITY_H, true, true, bold, null);
        } else {
            bf     = com.itextpdf.text.pdf.BaseFont.createFont(com.itextpdf.text.pdf.BaseFont.HELVETICA,      "Cp1252", false);
            bfBold = com.itextpdf.text.pdf.BaseFont.createFont(com.itextpdf.text.pdf.BaseFont.HELVETICA_BOLD, "Cp1252", false);
        }
        com.itextpdf.text.Font titleFont    = new com.itextpdf.text.Font(bfBold, 16);
        com.itextpdf.text.Font subtitleFont = new com.itextpdf.text.Font(bf, 10, com.itextpdf.text.Font.NORMAL, BaseColor.GRAY);
        com.itextpdf.text.Font headerFont   = new com.itextpdf.text.Font(bfBold, 9, com.itextpdf.text.Font.NORMAL, BaseColor.WHITE);
        com.itextpdf.text.Font cellFont     = new com.itextpdf.text.Font(bf, 9);
        com.itextpdf.text.Font boldFont     = new com.itextpdf.text.Font(bfBold, 9);

        // Title
        Paragraph title = new Paragraph("Звіт: Критичні залишки та рекомендації EOQ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new Paragraph("Дата: " + LocalDate.now() + "   |   Позицій: " + data.size(), subtitleFont));
        doc.add(Chunk.NEWLINE);

        // Table
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 1.5f, 2f, 1.2f, 1.2f, 1.2f, 4f});

        BaseColor headerBg = new BaseColor(15, 23, 42);
        String[] headers = {"Товар", "SKU", "Склад", "Залишок", "ROP", "EOQ", "Рекомендація"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        BaseColor altBg = new BaseColor(241, 245, 249);
        int i = 0;
        for (OptimizationRecommendation r : data) {
            BaseColor bg = (i % 2 == 0) ? BaseColor.WHITE : altBg;
            String[] vals = {
                r.getProductName(), r.getSku(),
                r.getWarehouseName() != null ? r.getWarehouseName() : "—",
                String.valueOf(r.getCurrentStock()),
                String.valueOf(r.getReorderPoint()),
                String.valueOf(r.getEoq()),
                r.getRecommendation() != null ? r.getRecommendation().split(":")[0] : "—"
            };
            for (int j = 0; j < vals.length; j++) {
                PdfPCell cell = new PdfPCell(new Phrase(vals[j], j == 3 && r.isNeedsReorder() ? boldFont : cellFont));
                cell.setBackgroundColor(bg);
                if (j == 3 && r.isNeedsReorder()) {
                    cell.setBackgroundColor(new BaseColor(254, 226, 226));
                }
                cell.setPadding(5);
                table.addCell(cell);
            }
            i++;
        }

        doc.add(table);
        doc.close();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reorder_alerts.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(out.toByteArray());
    }
}
