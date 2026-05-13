package com.inventory.forecast.methods;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.evaluation.ForecastAccuracyEvaluator;
import com.inventory.forecast.model.ForecastResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
 
@Component
@RequiredArgsConstructor
public class SimpleMovingAverageMethod implements ForecastingMethod {
 
    private final ForecastAccuracyEvaluator evaluator;
    private static final int DEFAULT_WINDOW = 3;
 
    @Override
    public String getMethodName() { return "SMA"; }
 
    @Override
    public ForecastResult forecast(List<DataPoint> history, int horizon) {
        int window = Math.min(DEFAULT_WINDOW, history.size());
 
        // 1. Розрахунок прогнозних значень
        List<DataPoint> forecastPoints = new ArrayList<>();
        List<DataPoint> working = new ArrayList<>(history);
 
        for (int i = 0; i < horizon; i++) {
            // Беремо останні 'window' значень
            List<Double> lastN = working.subList(working.size() - window, working.size())
                .stream().map(DataPoint::getValue).collect(Collectors.toList());
 
            double avg = lastN.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            LocalDate nextDate = working.get(working.size() - 1).getDate().plusDays(1);
 
            DataPoint next = new DataPoint(nextDate, avg);
            forecastPoints.add(next);
            working.add(next); // додаємо прогноз до робочого ряду
        }
 
        // 2. Оцінка точності на тренувальних даних (walk-forward)
        List<Double> actual = new ArrayList<>();
        List<Double> predicted = new ArrayList<>();
 
        for (int i = window; i < history.size(); i++) {
            List<Double> slice = history.subList(i - window, i)
                .stream().map(DataPoint::getValue).collect(Collectors.toList());
            double pred = slice.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            actual.add(history.get(i).getValue());
            predicted.add(pred);
        }
 
        double mae = actual.isEmpty() ? 0 : evaluator.mae(actual, predicted);
        double mape = actual.isEmpty() ? 0 : evaluator.mape(actual, predicted);
        double rmse = actual.isEmpty() ? 0 : evaluator.rmse(actual, predicted);
 
        return ForecastResult.builder()
            .method("SMA")
            .forecast(forecastPoints)
            .mae(mae).mape(mape).rmse(rmse)
            .description("Simple Moving Average (window=" + window + ")")
            .build();
    }
}