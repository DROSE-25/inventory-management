package com.inventory.service;

import com.inventory.dto.request.SaleRequest;
import com.inventory.dto.response.SaleImportResult;
import com.inventory.dto.response.SaleResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.mapper.SaleMapper;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.Warehouse;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final StockService stockService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public SaleResponse registerSale(SaleRequest dto) {
        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product", dto.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", dto.getWarehouseId()));

        stockService.deductStock(dto.getProductId(), dto.getWarehouseId(), dto.getQuantity());

        Sale sale = Sale.builder()
            .product(product)
            .warehouse(warehouse)
            .saleDate(dto.getSaleDate())
            .quantity(dto.getQuantity())
            .unitPrice(dto.getUnitPrice())
            .createdAt(OffsetDateTime.now())
            .build();

        return SaleMapper.toDTO(saleRepository.save(sale));
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByProductAndPeriod(Long productId, LocalDate from, LocalDate to) {
        return saleRepository
            .findByProductIdAndSaleDateBetweenOrderBySaleDateAsc(productId, from, to)
            .stream()
            .map(SaleMapper::toDTO)
            .toList();
    }

    // ─── НОВЫЕ МЕТОДЫ ────────────────────────────────────────────────────────

    public SaleImportResult importFromCsv(InputStream inputStream) {
        SaleImportResult result = new SaleImportResult();
        int row = 1;

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            List<CSVRecord> records = parser.getRecords();
            result.setTotalRows(records.size());

            for (CSVRecord record : records) {
                row++;
                try {
                    SaleRequest dto = SaleRequest.builder()
                        .productId(Long.parseLong(record.get("product_id")))
                        .warehouseId(Long.parseLong(record.get("warehouse_id")))
                        .saleDate(LocalDate.parse(record.get("sale_date")))
                        .quantity(new BigDecimal(record.get("quantity")))
                        .unitPrice(new BigDecimal(record.get("unit_price")))
                        .build();
                    registerSale(dto);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception e) {
                    result.addError(row, e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError(row, "Ошибка чтения файла: " + e.getMessage());
        }

        return result;
    }

    public SaleImportResult importFromExcel(InputStream inputStream) {
        SaleImportResult result = new SaleImportResult();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            // первая строка — заголовок, данные начинаются со строки 1
            int totalDataRows = sheet.getLastRowNum(); // lastRowNum исключает header
            result.setTotalRows(totalDataRows);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    SaleRequest dto = SaleRequest.builder()
                        .productId((long) row.getCell(0).getNumericCellValue())
                        .warehouseId((long) row.getCell(1).getNumericCellValue())
                        .saleDate(row.getCell(2).getLocalDateTimeCellValue().toLocalDate())
                        .quantity(BigDecimal.valueOf(row.getCell(3).getNumericCellValue()))
                        .unitPrice(BigDecimal.valueOf(row.getCell(4).getNumericCellValue()))
                        .build();
                    registerSale(dto);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception e) {
                    result.addError(i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            result.addError(0, "Ошибка чтения файла: " + e.getMessage());
        }

        return result;
    }
}