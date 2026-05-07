package com.inventory.service;
 
import com.inventory.dto.SalesAggregationPoint;
import com.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
 
import java.math.BigDecimal;
import java.sql.Timestamp;
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
            .map(row -> SalesAggregationPoint.builder()
                .periodStart(((java.time.LocalDateTime) ((java.sql.Timestamp) 
                java.sql.Timestamp.from((java.time.Instant) row[0])).toLocalDateTime()).toLocalDate())
                .totalQuantity((BigDecimal) row[1])
                .totalRevenue((BigDecimal) row[2])
                .build())
            .toList();
    }
}
