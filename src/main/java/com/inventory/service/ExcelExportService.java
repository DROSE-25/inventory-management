package com.inventory.service;

import com.inventory.model.AbcXyzResult;
import com.inventory.repository.AbcXyzResultRepository;
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

    @Transactional(readOnly = true)
    public byte[] exportAbcXyz() throws IOException {
        List<AbcXyzResult> data = abcXyzResultRepository.findAllByOrderByRevenueDesc();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("ABC-XYZ Аналіз");

            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] cols = {"ID","Назва","SKU","ABC","XYZ","Клас","Оборот","Частка %","CV %","Рекомендація"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }
            sheet.setColumnWidth(9, 12000);

            int rowNum = 1;
            for (AbcXyzResult r : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getProduct().getId());
                row.createCell(1).setCellValue(r.getProduct().getName());
                row.createCell(2).setCellValue(r.getProduct().getSku());
                row.createCell(3).setCellValue(r.getAbcClass());
                row.createCell(4).setCellValue(r.getXyzClass());
                row.createCell(5).setCellValue(r.getCombinedClass());
                row.createCell(6).setCellValue(r.getRevenue() != null ? r.getRevenue().doubleValue() : 0);
                row.createCell(7).setCellValue(r.getRevenueShare() != null ? r.getRevenueShare().multiply(java.math.BigDecimal.valueOf(100)).doubleValue() : 0);
                row.createCell(8).setCellValue(r.getCv() != null ? r.getCv().doubleValue() : 0);
                row.createCell(9).setCellValue("Дивись рекомендацію");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}