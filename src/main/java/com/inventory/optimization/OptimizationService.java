package com.inventory.optimization;

import com.inventory.forecast.ForecastService;
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.model.ForecastResult;
import com.inventory.model.Product;
import com.inventory.optimization.dto.OptimizationRecommendation;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.StockLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimizationService {

    private final ForecastService          forecastService;
    private final EOQCalculator             eoqCalculator;
    private final SafetyStockCalculator     safetyStockCalculator;
    private final ReorderPointCalculator    ropCalculator;
    private final ProductRepository         productRepository;
    private final SaleRepository            saleRepository;
    private final StockLevelRepository      stockLevelRepository;

    private static final double DEFAULT_SERVICE_LEVEL = 0.95;
    // Фолбек: якщо даних продажів немає — вважаємо попит 1 од./день
    private static final double FALLBACK_DAILY_DEMAND = 1.0;

    @Transactional(readOnly = true)
    public OptimizationRecommendation getRecommendation(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Product not found: " + productId));

        // 1. Завантажуємо history продажів за 90 днів як List<DataPoint>
        LocalDate toDate   = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(90);

        List<DataPoint> history = saleRepository
            .findByProductIdAndSaleDateBetweenOrderBySaleDateAsc(productId, fromDate, toDate)
            .stream()
            .map(sale -> new DataPoint(
                sale.getSaleDate(),
                sale.getQuantity().doubleValue()))
            .collect(Collectors.toList());

        List<Double> demandHistory = history.stream()
            .map(DataPoint::getValue)
            .collect(Collectors.toList());

        // 2. Прогноз попиту — якщо даних немає, використовуємо фолбек
        double forecastedMonthlyDemand;
        String forecastMethod;
        double forecastMape;

        if (history.size() >= 2) {
            ForecastResult forecast = forecastService.getBestForecast(history, 30);
            forecastedMonthlyDemand = forecast.getForecast().stream()
                .mapToDouble(DataPoint::getValue)
                .sum();
            forecastMethod = forecast.getMethod();
            forecastMape   = forecast.getMape();
        } else {
            log.warn("Not enough sales history for product {} (found {}), using fallback demand",
                productId, history.size());
            forecastedMonthlyDemand = FALLBACK_DAILY_DEMAND * 30;
            forecastMethod = "FALLBACK";
            forecastMape   = Double.NaN;
        }

        // Захист від нульового прогнозу
        if (forecastedMonthlyDemand <= 0) {
            forecastedMonthlyDemand = FALLBACK_DAILY_DEMAND * 30;
        }

        // 3. Safety Stock — потрібно мінімум 2 спостереження
        int leadTime = product.getSupplier().getLeadTimeDays();
        int ss;
        if (demandHistory.size() >= 2) {
            ss = safetyStockCalculator.calculate(demandHistory, leadTime, DEFAULT_SERVICE_LEVEL);
        } else {
            // Фолбек: SS = середній щоденний попит * leadTime * z(95%)
            ss = (int) Math.ceil(FALLBACK_DAILY_DEMAND * leadTime * 1.65);
        }

        // 4. EOQ
        double annualDemand = forecastedMonthlyDemand * 12;
        double orderingCost = product.getOrderingCost().doubleValue();
        double holdingCost  = product.getUnitPrice().doubleValue() * 0.25;
        int eoq = eoqCalculator.calculate(annualDemand, orderingCost, holdingCost);

        // 5. ROP
        int rop = ropCalculator.calculateFromMonthlyDemand(
            forecastedMonthlyDemand, leadTime, ss);

        // 6. Поточний залишок — сума по всіх складах; окремо визначаємо склад з найнижчим залишком
        List<com.inventory.model.StockLevel> productStockLevels =
            stockLevelRepository.findByProductIdWithWarehouse(productId);

        double currentStock = productStockLevels.stream()
            .mapToDouble(sl -> sl.getQuantity().doubleValue())
            .sum();

        String warehouseName = productStockLevels.stream()
            .min(java.util.Comparator.comparingDouble(sl -> sl.getQuantity().doubleValue()))
            .map(sl -> sl.getWarehouse().getName())
            .orElse("—");

        boolean needsReorder = currentStock <= rop;

        String recommendation;
        if (currentStock <= ss) {
            recommendation = "КРИТИЧНО: Залишок нижче страхового запасу! " +
                "Розмістіть ТЕРМІНОВЕ замовлення на " + eoq + " одиниць.";
        } else if (needsReorder) {
            recommendation = "РЕКОМЕНДУЄТЬСЯ ЗАМОВЛЕННЯ: Залишок досяг точки " +
                "перезамовлення. Замовте " + eoq + " одиниць.";
        } else {
            double dailyDemand = forecastedMonthlyDemand / 30.0;
            int daysLeft = (int) ((currentStock - rop) / dailyDemand);
            recommendation = "Запас достатній. Замовлення через приблизно " +
                daysLeft + " днів.";
        }

        double totalCost = eoqCalculator.totalAnnualCost(
            annualDemand, orderingCost, holdingCost);

        return OptimizationRecommendation.builder()
            .productId(productId)
            .productName(product.getName())
            .sku(product.getSku())
            .warehouseName(warehouseName)
            .forecastMethod(forecastMethod)
            .forecastedMonthlyDemand(forecastedMonthlyDemand)
            .forecastMape(forecastMape)
            .eoq(eoq)
            .safetyStock(ss)
            .reorderPoint(rop)
            .currentStock((int) currentStock)
            .needsReorder(needsReorder)
            .recommendation(recommendation)
            .totalAnnualCost(totalCost)
            .build();
    }
}