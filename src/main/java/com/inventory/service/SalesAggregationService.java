package com.inventory.service;

import com.inventory.dto.SalesAggregationPoint;
import com.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesAggregationService {

    private final SaleRepository saleRepo;

    public List<SalesAggregationPoint> aggregate(
            Long productId,
            Long warehouseId,
            LocalDate from,
            LocalDate to,
            String granularity) {

        if (!List.of("day", "week", "month").contains(granularity)) {
            throw new IllegalArgumentException(
                "granularity має бути одне з: day, week, month");
        }

        return saleRepo.aggregateByPeriod(granularity, productId, warehouseId, from, to)
            .stream()
            .map(row -> {
                LocalDate date;
                Object col0 = row[0];
                if (col0 instanceof java.sql.Date d) {
                    date = d.toLocalDate();
                } else if (col0 instanceof java.sql.Timestamp ts) {
                    date = ts.toLocalDateTime().toLocalDate();
                } else if (col0 instanceof LocalDate ld) {
                    date = ld;
                } else {
                    date = LocalDate.parse(col0.toString().substring(0, 10));
                }
                return SalesAggregationPoint.builder()
                    .periodStart(date)
                    .totalQuantity((BigDecimal) row[1])
                    .totalRevenue((BigDecimal) row[2])
                    .build();
            })
            .toList();
    }
}