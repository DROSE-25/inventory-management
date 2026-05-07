package com.inventory.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleImportResult {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private List<String> errors; // Список сообщений об ошибках (например, "Строка 5: Товар не найден")

    // Удобный конструктор для быстрого создания результата
    public static SaleImportResult summary(int total, int success, List<String> errors) {
        return new SaleImportResult(total, success, errors.size(), errors);
    }
}