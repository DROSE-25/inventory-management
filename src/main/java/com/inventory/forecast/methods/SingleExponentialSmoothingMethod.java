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
public class SingleExponentialSmoothingMethod implements ForecastingMethod {
 
    private final ForecastAccuracyEvaluator evaluator;
 
    @Override
    public String getMethodName() { return "SES"; }
 
    @Override
    public ForecastResult forecast(List<DataPoint> history, int horizon) {
        if (history.size() < 2)
            throw new IllegalArgumentException("SES requires at least 2 data points");
 
        // Автопідбір оптимального alpha
        double bestAlpha = findBestAlpha(history);
 
        // Метрики на тестовій вибірці (split 80/20)
        int splitIdx = Math.max(2, (int)(history.size() * 0.8));
        List<DataPoint> train = history.subList(0, splitIdx);
        List<DataPoint> test  = history.subList(splitIdx, history.size());
        List<Double> actual    = test.stream().map(DataPoint::getValue).collect(Collectors.toList());
        List<Double> predicted = applyMakesSES(train, test.size(), bestAlpha);
 
        double mae  = actual.isEmpty() ? 0 : evaluator.mae(actual, predicted);
        double mape = actual.isEmpty() ? 0 : evaluator.mape(actual, predicted);
        double rmse = actual.isEmpty() ? 0 : evaluator.rmse(actual, predicted);
 
        // Прогноз на horizon кроків
        List<DataPoint> forecastPoints = generateForecast(history, horizon, bestAlpha);
 
        return ForecastResult.builder()
            .method("SES")
            .forecast(forecastPoints)
            .mae(mae).mape(mape).rmse(rmse)
            .description(String.format("Single Exponential Smoothing (alpha=%.2f)", bestAlpha))
            .build();
    }
 
    // Перебираємо alpha від 0.1 до 0.9 з кроком 0.1, беремо мінімум MAPE
    private double findBestAlpha(List<DataPoint> history) {
        double bestAlpha = 0.3;
        double bestMape = Double.MAX_VALUE;
        int split = Math.max(2, (int)(history.size() * 0.8));
        List<DataPoint> train = history.subList(0, split);
        List<DataPoint> test  = history.subList(split, history.size());
        if (test.isEmpty()) return bestAlpha;
        List<Double> actual = test.stream().map(DataPoint::getValue).collect(Collectors.toList());
        for (int i = 1; i <= 9; i++) {
            double alpha = i * 0.1;
            List<Double> preds = applyMakesSES(train, test.size(), alpha);
            double mape = evaluator.mape(actual, preds);
            if (mape < bestMape) { bestMape = mape; bestAlpha = alpha; }
        }
        return bestAlpha;
    }
 
    private List<Double> applyMakesSES(List<DataPoint> data, int steps, double alpha) {
        List<Double> vals = data.stream().map(DataPoint::getValue).collect(Collectors.toList());
        double s = vals.get(0); // S(1) = X(1)
        for (int i = 1; i < vals.size(); i++)
            s = alpha * vals.get(i) + (1 - alpha) * s;
        List<Double> preds = new ArrayList<>();
        for (int i = 0; i < steps; i++) preds.add(s); // SES: всі прогнози = S(t)
        return preds;
    }
 
    private List<DataPoint> generateForecast(List<DataPoint> history, int horizon, double alpha) {
        List<Double> vals = history.stream().map(DataPoint::getValue).collect(Collectors.toList());
        double s = vals.get(0);
        for (int i = 1; i < vals.size(); i++)
            s = alpha * vals.get(i) + (1 - alpha) * s;
        LocalDate last = history.get(history.size() - 1).getDate();
        List<DataPoint> result = new ArrayList<>();
        for (int i = 1; i <= horizon; i++)
            result.add(new DataPoint(last.plusDays(i), s));
        return result;
    }
}
