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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SaleService {

    // Константи для колонок Excel
    private static final int EXCEL_COL_PRODUCT_ID = 0;
    private static final int EXCEL_COL_WAREHOUSE_ID = 1;
    private static final int EXCEL_COL_SALE_DATE = 2;
    private static final int EXCEL_COL_QUANTITY = 3;
    private static final int EXCEL_COL_UNIT_PRICE = 4;

    private final SaleRepository saleRepository;
    private final StockService stockService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public SaleResponse registerSale(SaleRequest dto) {
        log.info("Реєстрація продажу: товар ID={}, склад ID={}, кількість={}", 
                dto.getProductId(), dto.getWarehouseId(), dto.getQuantity());

        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Товар", dto.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Склад", dto.getWarehouseId()));

        // Зменшуємо залишки на складі
        stockService.deductStock(dto.getProductId(), dto.getWarehouseId(), dto.getQuantity());

        Sale sale = Sale.builder()
            .product(product)
            .warehouse(warehouse)
            .saleDate(dto.getSaleDate())
            .quantity(dto.getQuantity())
            .unitPrice(dto.getUnitPrice())
            .createdAt(OffsetDateTime.now())
            .build();

        Sale savedSale = saleRepository.save(sale);
        log.debug("Продаж успішно зареєстровано, ID запису: {}", savedSale.getId());
        
        return SaleMapper.toDTO(savedSale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByProductAndPeriod(Long productId, LocalDate from, LocalDate to) {
        log.debug("Отримання історії продажів для товару ID={} з {} по {}", productId, from, to);
        return saleRepository
            .findByProductIdAndSaleDateBetweenOrderBySaleDateAsc(productId, from, to)
            .stream()
            .map(SaleMapper::toDTO)
            .toList();
    }

    public SaleImportResult importFromCsv(InputStream inputStream) {
        log.info("Початок імпорту продажів з CSV файлу");
        SaleImportResult result = new SaleImportResult();
        int row = 1;

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            List<CSVRecord> records = parser.getRecords();
            result.setTotalRows(records.size());
            log.info("Знайдено {} рядків для імпорту", records.size());

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
                    log.warn("Помилка імпорту CSV у рядку {}: {}", row, e.getMessage());
                    result.addError(row, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Критична помилка читання CSV файлу: {}", e.getMessage());
            result.addError(row, "Помилка читання файлу: " + e.getMessage());
        }

        log.info("Імпорт з CSV завершено. Успішно: {}/{}", result.getSuccessCount(), result.getTotalRows());
        return result;
    }

    public SaleImportResult importFromExcel(InputStream inputStream) {
        log.info("Початок імпорту продажів з Excel файлу");
        SaleImportResult result = new SaleImportResult();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalDataRows = sheet.getLastRowNum();
            result.setTotalRows(totalDataRows);
            log.info("Знайдено {} рядків для імпорту в Excel", totalDataRows);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    SaleRequest dto = SaleRequest.builder()
                        .productId((long) row.getCell(EXCEL_COL_PRODUCT_ID).getNumericCellValue())
                        .warehouseId((long) row.getCell(EXCEL_COL_WAREHOUSE_ID).getNumericCellValue())
                        .saleDate(row.getCell(EXCEL_COL_SALE_DATE).getLocalDateTimeCellValue().toLocalDate())
                        .quantity(BigDecimal.valueOf(row.getCell(EXCEL_COL_QUANTITY).getNumericCellValue()))
                        .unitPrice(BigDecimal.valueOf(row.getCell(EXCEL_COL_UNIT_PRICE).getNumericCellValue()))
                        .build();
                        
                    registerSale(dto);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                } catch (Exception e) {
                    log.warn("Помилка імпорту Excel у рядку {}: {}", i + 1, e.getMessage());
                    result.addError(i + 1, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Критична помилка читання Excel файлу: {}", e.getMessage());
            result.addError(0, "Помилка читання файлу: " + e.getMessage());
        }

        log.info("Імпорт з Excel завершено. Успішно: {}/{}", result.getSuccessCount(), result.getTotalRows());
        return result;
    }
}