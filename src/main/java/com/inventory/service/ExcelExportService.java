package com.inventory.service;

import com.inventory.model.AbcXyzResult;
import com.inventory.repository.AbcXyzResultRepository;
import com.inventory.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final AbcXyzResultRepository abcXyzResultRepository;
    private final SecurityUtils          securityUtils;

    @Transactional(readOnly = true)
    public byte[] exportAbcXyz() throws IOException {
        Long companyId = securityUtils.getCurrentCompanyId();
        List<AbcXyzResult> data = abcXyzResultRepository.findAllByCompanyIdOrderByRevenueDesc(companyId);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("ABC-XYZ Аналіз");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Wrap style for recommendation column
            CellStyle wrapStyle = wb.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // Alt row wrap style
            CellStyle wrapAltStyle = wb.createCellStyle();
            wrapAltStyle.setWrapText(true);
            wrapAltStyle.setVerticalAlignment(VerticalAlignment.TOP);
            wrapAltStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            wrapAltStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Назва", "SKU", "ABC", "XYZ", "Клас", "Оборот", "Частка %", "CV %", "Рекомендація"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // Column widths
            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 3500);
            sheet.setColumnWidth(3, 2000);
            sheet.setColumnWidth(4, 2000);
            sheet.setColumnWidth(5, 2500);
            sheet.setColumnWidth(6, 4000);
            sheet.setColumnWidth(7, 3500);
            sheet.setColumnWidth(8, 3000);
            sheet.setColumnWidth(9, 20000);

            int rowNum = 1;
            for (AbcXyzResult r : data) {
                Row row = sheet.createRow(rowNum);
                row.setHeight((short) 800);

                boolean isAlt = rowNum % 2 == 0;
                CellStyle recStyle = isAlt ? wrapAltStyle : wrapStyle;

                row.createCell(0).setCellValue(r.getProduct().getId());
                row.createCell(1).setCellValue(r.getProduct().getName());
                row.createCell(2).setCellValue(r.getProduct().getSku());
                row.createCell(3).setCellValue(r.getAbcClass());
                row.createCell(4).setCellValue(r.getXyzClass());
                row.createCell(5).setCellValue(r.getCombinedClass());
                row.createCell(6).setCellValue(r.getRevenue() != null
                    ? r.getRevenue().doubleValue() : 0);
                row.createCell(7).setCellValue(r.getRevenueShare() != null
                    ? r.getRevenueShare().multiply(java.math.BigDecimal.valueOf(100)).doubleValue() : 0);
                row.createCell(8).setCellValue(r.getCv() != null
                    ? r.getCv().doubleValue() : 0);

                String combined = r.getCombinedClass() != null ? r.getCombinedClass() : "";
                String rec;
                if (combined.startsWith("A"))
                    rec = "Пріоритетний товар. Постійний контроль залишків, мінімальний страховий запас.";
                else if (combined.startsWith("B"))
                    rec = "Середній пріоритет. Регулярний моніторинг, помірний страховий запас.";
                else
                    rec = "Низький пріоритет. Можливе скорочення асортименту або замовлення за потребою.";

                if (combined.endsWith("Z"))
                    rec += " Попит нестабільний — замовляти обережно.";
                else if (combined.endsWith("X"))
                    rec += " Попит стабільний — можна планувати автоматично.";

                Cell recCell = row.createCell(9);
                recCell.setCellValue(rec);
                recCell.setCellStyle(recStyle);

                rowNum++;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
