package com.inventory.controller;

import com.inventory.forecast.ForecastService;
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.model.ForecastResult;
import com.inventory.optimization.OptimizationService;
import com.inventory.optimization.dto.OptimizationRecommendation;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forecast-export")
@RequiredArgsConstructor
@Tag(name = "Forecast Export", description = "Експорт прогнозів")
public class ForecastExportController {

    private final OptimizationService optimizationService;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final ForecastService forecastService;

    // ── Style helpers ─────────────────────────────────────────────────────────

    private CellStyle makeHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle makeTitleStyle(Workbook wb, short size) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints(size);
        s.setFont(f);
        return s;
    }

    private CellStyle makeAltStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle makeLabelStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle makeGreenStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle makeOrangeStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "—");
        if (style != null) cell.setCellStyle(style);
    }

    private void setNumCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    // ── 1. Критичні залишки ───────────────────────────────────────────────────

    @Operation(summary = "Завантажити критичні залишки у Excel")
    @GetMapping("/excel/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','MANAGER')")
    public ResponseEntity<byte[]> downloadReorderExcel() throws Exception {
        List<OptimizationRecommendation> data = productRepository.findAll().stream()
            .map(p -> optimizationService.getRecommendation(p.getId()))
            .filter(OptimizationRecommendation::isNeedsReorder)
            .collect(Collectors.toList());

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Критичні залишки");
            CellStyle hdr = makeHeaderStyle(wb);
            CellStyle ttl = makeTitleStyle(wb, (short) 14);
            CellStyle alt = makeAltStyle(wb);

            Row titleRow = sheet.createRow(0);
            Cell tc = titleRow.createCell(0);
            tc.setCellValue("Звіт: Критичні залишки та рекомендації EOQ");
            tc.setCellStyle(ttl);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Дата звіту: " + LocalDate.now());
            dateRow.createCell(4).setCellValue("Всього позицій: " + data.size());
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
            sheet.createRow(2);

            String[] cols = {"Товар", "SKU", "Склад", "Залишок", "ROP", "EOQ", "Страх. запас", "Рекомендація"};
            Row header = sheet.createRow(3);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i); cell.setCellValue(cols[i]); cell.setCellStyle(hdr);
                sheet.setColumnWidth(i, i == 7 ? 15000 : 4500);
            }

            int rowNum = 4;
            for (OptimizationRecommendation r : data) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? alt : null;
                String[] vals = { r.getProductName(), r.getSku(),
                    r.getWarehouseName() != null ? r.getWarehouseName() : "—",
                    String.valueOf(r.getCurrentStock()), String.valueOf(r.getReorderPoint()),
                    String.valueOf(r.getEoq()), String.valueOf(r.getSafetyStock()), r.getRecommendation() };
                for (int i = 0; i < vals.length; i++) setCell(row, i, vals[i], style);
                rowNum++;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=reorder_alerts_" + LocalDate.now() + ".xlsx")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
        }
    }

    // ── 2. Прогноз конкретного товару ─────────────────────────────────────────

    @Operation(summary = "Завантажити прогноз для конкретного товару у Excel")
    @GetMapping("/excel/forecast/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','MANAGER')")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadForecastExcel(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") int horizon) throws Exception {

        var product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(90);

        List<DataPoint> history = saleRepository
            .findByProductIdAndSaleDateBetweenOrderBySaleDateAsc(productId, from, to)
            .stream()
            .map(s -> new DataPoint(s.getSaleDate(), s.getQuantity().doubleValue()))
            .collect(Collectors.toList());

        if (history.size() < 2) return ResponseEntity.badRequest().build();

        // Run all methods
        List<ForecastResult> allResults = forecastService.getAvailableMethods().stream()
            .map(name -> {
                try { return forecastService.runForecast(name, history, horizon); }
                catch (Exception e) { return null; }
            })
            .filter(r -> r != null && !Double.isNaN(r.getMape()))
            .sorted(Comparator.comparingDouble(ForecastResult::getMape))
            .collect(Collectors.toList());

        ForecastResult best = allResults.isEmpty() ? null : allResults.get(0);

        // Optimization data
        OptimizationRecommendation opt = optimizationService.getRecommendation(productId);

        // History stats
        DoubleSummaryStatistics stats = history.stream()
            .mapToDouble(DataPoint::getValue).summaryStatistics();
        double avg = stats.getAverage();
        double min = stats.getMin();
        double max = stats.getMax();
        double stdDev = Math.sqrt(history.stream()
            .mapToDouble(dp -> Math.pow(dp.getValue() - avg, 2)).average().orElse(0));

        // Trend
        double totalPeriod = best != null && best.getForecast() != null
            ? best.getForecast().stream().mapToDouble(DataPoint::getValue).sum() : 0;
        String trendText = stdDev / avg < 0.15 ? "Стабільний попит"
            : stdDev / avg < 0.35 ? "Помірна нестабільність" : "Нестабільний попит";

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle hdr  = makeHeaderStyle(wb);
            CellStyle ttl  = makeTitleStyle(wb, (short) 14);
            CellStyle ttl2 = makeTitleStyle(wb, (short) 11);
            CellStyle alt  = makeAltStyle(wb);
            CellStyle lbl  = makeLabelStyle(wb);
            CellStyle grn  = makeGreenStyle(wb);
            CellStyle org  = makeOrangeStyle(wb);

            // ── Аркуш 1: Зведення ────────────────────────────────────────────
            Sheet s1 = wb.createSheet("📋 Зведення");

            int r = 0;
            Row tr = s1.createRow(r++);
            Cell tc2 = tr.createCell(0);
            tc2.setCellValue("ЗВІТ ПРОГНОЗУВАННЯ: " + product.getName().toUpperCase());
            tc2.setCellStyle(ttl);
            s1.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            s1.createRow(r++).createCell(0)
                .setCellValue("Товар: " + product.getName() + "  |  SKU: " + product.getSku()
                    + "  |  Горизонт: " + horizon + " днів  |  Дата: " + LocalDate.now());
            s1.createRow(r++);

            // Блок 1: Прогноз
            Row b1 = s1.createRow(r++);
            Cell b1c = b1.createCell(0); b1c.setCellValue("ПРОГНОЗ ПРОДАЖІВ"); b1c.setCellStyle(hdr);
            s1.addMergedRegion(new CellRangeAddress(r-1, r-1, 0, 3));

            String[][] forecastRows = {
                {"Рекомендований метод", best != null ? best.getMethod() : "—"},
                {"Точність (MAPE)", best != null ? String.format("%.1f%%", best.getMape()) : "—"},
                {"MAE (середня абс. помилка)", best != null ? String.format("%.1f шт", best.getMae()) : "—"},
                {"RMSE", best != null ? String.format("%.1f", best.getRmse()) : "—"},
                {"Прогноз на " + horizon + " днів (всього)", String.format("%.0f шт", totalPeriod)},
                {"Прогноз на тиждень (середнє)", String.format("%.0f шт/тиж", totalPeriod / (horizon / 7.0))},
                {"Прогноз на день (середнє)", String.format("%.1f шт/день", totalPeriod / horizon)},
            };
            for (String[] pair : forecastRows) {
                Row row = s1.createRow(r++);
                Cell c0 = row.createCell(0); c0.setCellValue(pair[0]); c0.setCellStyle(lbl);
                row.createCell(1).setCellValue(pair[1]);
            }
            s1.createRow(r++);

            // Блок 2: Статистика
            Row b2 = s1.createRow(r++);
            Cell b2c = b2.createCell(0); b2c.setCellValue("СТАТИСТИКА (90 ДНІВ)"); b2c.setCellStyle(hdr);
            s1.addMergedRegion(new CellRangeAddress(r-1, r-1, 0, 3));

            String[][] statsRows = {
                {"Кількість записів продажів", String.valueOf(history.size())},
                {"Середні продажі/день", String.format("%.1f шт", avg)},
                {"Мінімум за день", String.format("%.0f шт", min)},
                {"Максимум за день", String.format("%.0f шт", max)},
                {"Стандартне відхилення", String.format("%.1f шт", stdDev)},
                {"Коефіцієнт варіації (CV)", String.format("%.1f%%", (stdDev / avg) * 100)},
                {"Характер попиту", trendText},
            };
            for (String[] pair : statsRows) {
                Row row = s1.createRow(r++);
                Cell c0 = row.createCell(0); c0.setCellValue(pair[0]); c0.setCellStyle(lbl);
                row.createCell(1).setCellValue(pair[1]);
            }
            s1.createRow(r++);

            // Блок 3: Оптимізація запасів
            Row b3 = s1.createRow(r++);
            Cell b3c = b3.createCell(0); b3c.setCellValue("ОПТИМІЗАЦІЯ ЗАПАСІВ (EOQ/ROP)"); b3c.setCellStyle(hdr);
            s1.addMergedRegion(new CellRangeAddress(r-1, r-1, 0, 3));

            boolean needsReorder = opt != null && opt.isNeedsReorder();
            String[][] optRows = {
                {"Поточний залишок", opt != null ? opt.getCurrentStock() + " шт" : "—"},
                {"Точка перезамовлення (ROP)", opt != null ? opt.getReorderPoint() + " шт" : "—"},
                {"Оптимальний обсяг замовлення (EOQ)", opt != null ? opt.getEoq() + " шт" : "—"},
                {"Страховий запас", opt != null ? opt.getSafetyStock() + " шт" : "—"},
                {"Статус", needsReorder ? "⚠ ПОТРІБНЕ ЗАМОВЛЕННЯ" : "✓ Запас достатній"},
                {"Рекомендація", opt != null && opt.getRecommendation() != null ? opt.getRecommendation() : "—"},
            };
            for (String[] pair : optRows) {
                Row row = s1.createRow(r++);
                Cell c0 = row.createCell(0); c0.setCellValue(pair[0]); c0.setCellStyle(lbl);
                Cell c1 = row.createCell(1); c1.setCellValue(pair[1]);
                if (pair[0].equals("Статус")) {
                    c1.setCellStyle(needsReorder ? org : grn);
                }
            }

            s1.setColumnWidth(0, 10000);
            s1.setColumnWidth(1, 8000);
            s1.setColumnWidth(2, 4000);
            s1.setColumnWidth(3, 4000);

            // ── Аркуш 2: Прогноз по датах ────────────────────────────────────
            if (best != null && best.getForecast() != null) {
                Sheet s2 = wb.createSheet("📈 Прогноз по датах");
                Row t2 = s2.createRow(0);
                Cell tc3 = t2.createCell(0);
                tc3.setCellValue("Прогноз: " + product.getName() + " | Метод: " + best.getMethod()
                    + " | MAPE: " + String.format("%.1f%%", best.getMape()));
                tc3.setCellStyle(ttl2);
                s2.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
                s2.createRow(1);

                String[] cols2 = {"Дата", "Прогноз (шт)", "Накопичений підсумок (шт)"};
                Row h2 = s2.createRow(2);
                for (int i = 0; i < cols2.length; i++) {
                    Cell c = h2.createCell(i); c.setCellValue(cols2[i]); c.setCellStyle(hdr);
                }
                s2.setColumnWidth(0, 4000); s2.setColumnWidth(1, 4000); s2.setColumnWidth(2, 6000);

                List<DataPoint> pts = best.getForecast();
                double cumulative = 0;
                for (int i = 0; i < pts.size(); i++) {
                    Row row = s2.createRow(3 + i);
                    CellStyle st = (i % 2 == 0) ? alt : null;
                    DataPoint dp = pts.get(i);
                    cumulative += dp.getValue();
                    setCell(row, 0, dp.getDate() != null ? dp.getDate().toString() : "День " + (i + 1), st);
                    setCell(row, 1, String.format("%.0f", dp.getValue()), st);
                    setCell(row, 2, String.format("%.0f", cumulative), st);
                }
            }

            // ── Аркуш 3: Порівняння методів ──────────────────────────────────
            Sheet s3 = wb.createSheet("🔬 Порівняння методів");
            Row t3 = s3.createRow(0);
            Cell tc4 = t3.createCell(0);
            tc4.setCellValue("Порівняння методів: " + product.getName());
            tc4.setCellStyle(ttl2);
            s3.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            s3.createRow(1);

            String[] cols3 = {"Метод", "MAE", "MAPE (%)", "RMSE", "Прогноз (сума)", "Статус"};
            Row h3 = s3.createRow(2);
            for (int i = 0; i < cols3.length; i++) {
                Cell c = h3.createCell(i); c.setCellValue(cols3[i]); c.setCellStyle(hdr);
            }
            s3.setColumnWidth(0, 6000); s3.setColumnWidth(1, 3500); s3.setColumnWidth(2, 3500);
            s3.setColumnWidth(3, 3500); s3.setColumnWidth(4, 5000); s3.setColumnWidth(5, 5000);

            for (int i = 0; i < allResults.size(); i++) {
                ForecastResult res = allResults.get(i);
                Row row = s3.createRow(3 + i);
                CellStyle st = (i % 2 == 0) ? alt : null;
                boolean isBest = best != null && res.getMethod().equals(best.getMethod());
                double forecastSum = res.getForecast() != null
                    ? res.getForecast().stream().mapToDouble(DataPoint::getValue).sum() : 0;
                setCell(row, 0, res.getMethod(), st);
                setCell(row, 1, String.format("%.2f", res.getMae()), st);
                setCell(row, 2, String.format("%.1f", res.getMape()), st);
                setCell(row, 3, String.format("%.2f", res.getRmse()), st);
                setCell(row, 4, String.format("%.0f шт", forecastSum), st);
                Cell statusCell = row.createCell(5);
                statusCell.setCellValue(isBest ? "✓ Найкращий" : "");
                if (isBest) statusCell.setCellStyle(grn);
            }

            // ── Аркуш 4: Історія продажів ────────────────────────────────────
            Sheet s4 = wb.createSheet("📦 Історія продажів");
            Row t4 = s4.createRow(0);
            Cell tc5 = t4.createCell(0);
            tc5.setCellValue("Історія продажів (90 днів): " + product.getName());
            tc5.setCellStyle(ttl2);
            s4.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            // Summary stats row
            s4.createRow(1).createCell(0).setCellValue(
                String.format("Середнє: %.1f  |  Мін: %.0f  |  Макс: %.0f  |  Стд. відхилення: %.1f  |  Всього записів: %d",
                    avg, min, max, stdDev, history.size()));
            s4.createRow(2);

            String[] cols4 = {"Дата", "Продано (шт)", "Відхилення від середнього"};
            Row h4 = s4.createRow(3);
            for (int i = 0; i < cols4.length; i++) {
                Cell c = h4.createCell(i); c.setCellValue(cols4[i]); c.setCellStyle(hdr);
            }
            s4.setColumnWidth(0, 4000); s4.setColumnWidth(1, 4000); s4.setColumnWidth(2, 7000);

            for (int i = 0; i < history.size(); i++) {
                Row row = s4.createRow(4 + i);
                CellStyle st = (i % 2 == 0) ? alt : null;
                DataPoint dp = history.get(i);
                double deviation = dp.getValue() - avg;
                setCell(row, 0, dp.getDate() != null ? dp.getDate().toString() : "—", st);
                setCell(row, 1, String.format("%.0f", dp.getValue()), st);
                setCell(row, 2, String.format("%+.1f шт (%.0f%%)", deviation, (deviation / avg) * 100), st);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);

            String filename = "forecast_" + product.getName()
                .replaceAll("[^а-яА-ЯіїєІЇЄa-zA-Z0-9]", "_") + "_" + LocalDate.now() + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
        }
    }
}