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
public class WeightedMovingAverageMethod implements ForecastingMethod {
 
    private final ForecastAccuracyEvaluator evaluator;
    private static final int DEFAULT_WINDOW = 3;
 
    @Override
    public String getMethodName() { return "WMA"; }
 
    @Override
    public ForecastResult forecast(List<DataPoint> history, int horizon) {
        int window = Math.min(DEFAULT_WINDOW, history.size());
 
        List<DataPoint> forecastPoints = new ArrayList<>();
        List<DataPoint> working = new ArrayList<>(history);
 
        for (int i = 0; i < horizon; i++) {
            List<Double> lastN = working.subList(working.size() - window, working.size())
                .stream().map(DataPoint::getValue).collect(Collectors.toList());
 
            double wma = computeWMA(lastN, window);
            LocalDate nextDate = working.get(working.size() - 1).getDate().plusDays(1);
 
            DataPoint next = new DataPoint(nextDate, wma);
            forecastPoints.add(next);
            working.add(next);
        }
 
        // Оцінка точності
        List<Double> actual = new ArrayList<>();
        List<Double> predicted = new ArrayList<>();
 
        for (int i = window; i < history.size(); i++) {
            List<Double> slice = history.subList(i - window, i)
                .stream().map(DataPoint::getValue).collect(Collectors.toList());
            actual.add(history.get(i).getValue());
            predicted.add(computeWMA(slice, window));
        }
 
        double mae = actual.isEmpty() ? 0 : evaluator.mae(actual, predicted);
        double mape = actual.isEmpty() ? 0 : evaluator.mape(actual, predicted);
        double rmse = actual.isEmpty() ? 0 : evaluator.rmse(actual, predicted);
 
        return ForecastResult.builder()
            .method("WMA")
            .forecast(forecastPoints)
            .mae(mae).mape(mape).rmse(rmse)
            .description("Weighted Moving Average (window=" + window + ")")
            .build();
    }
 
    // Лінійні ваги: 1, 2, 3, ..., window
    private double computeWMA(List<Double> values, int window) {
        double sum = 0;
        double weightSum = 0;
        for (int i = 0; i < values.size(); i++) {
            double weight = i + 1; // вага зростає для новіших даних
            sum += weight * values.get(i);
            weightSum += weight;
        }
        return sum / weightSum;
    }
}
