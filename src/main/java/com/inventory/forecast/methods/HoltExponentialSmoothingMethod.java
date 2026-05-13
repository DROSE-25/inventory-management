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
public class HoltExponentialSmoothingMethod implements ForecastingMethod {
 
    private final ForecastAccuracyEvaluator evaluator;
 
    @Override
    public String getMethodName() { return "HOLT"; }
 
    @Override
    public ForecastResult forecast(List<DataPoint> history, int horizon) {
        if (history.size() < 3)
            throw new IllegalArgumentException("Holt requires at least 3 data points");
 
        double[] best = findBestParams(history);
        double alpha = best[0], beta = best[1];
 
        int splitIdx = Math.max(3, (int)(history.size() * 0.8));
        List<DataPoint> train = history.subList(0, splitIdx);
        List<DataPoint> test  = history.subList(splitIdx, history.size());
        List<Double> actual    = test.stream().map(DataPoint::getValue).collect(Collectors.toList());
        List<Double> predicted = applyHolt(train, test.size(), alpha, beta);
 
        double mae  = actual.isEmpty() ? 0 : evaluator.mae(actual, predicted);
        double mape = actual.isEmpty() ? 0 : evaluator.mape(actual, predicted);
        double rmse = actual.isEmpty() ? 0 : evaluator.rmse(actual, predicted);
 
        return ForecastResult.builder()
            .method("HOLT")
            .forecast(generateForecast(history, horizon, alpha, beta))
            .mae(mae).mape(mape).rmse(rmse)
            .description(String.format("Holt Double Exp Smoothing (alpha=%.2f, beta=%.2f)", alpha, beta))
            .build();
    }
 
    // Перебираємо alpha і beta від 0.1 до 0.9 з кроком 0.2 (81 комбінація)
    private double[] findBestParams(List<DataPoint> history) {
        double bestAlpha = 0.3, bestBeta = 0.1, bestMape = Double.MAX_VALUE;
        int split = Math.max(3, (int)(history.size() * 0.8));
        List<DataPoint> train = history.subList(0, split);
        List<DataPoint> test  = history.subList(split, history.size());
        if (test.isEmpty()) return new double[]{bestAlpha, bestBeta};
        List<Double> actual = test.stream().map(DataPoint::getValue).collect(Collectors.toList());
        for (int a = 1; a <= 9; a += 2) {
            for (int b = 1; b <= 9; b += 2) {
                double alpha = a * 0.1, beta = b * 0.1;
                List<Double> preds = applyHolt(train, test.size(), alpha, beta);
                double mape = evaluator.mape(actual, preds);
                if (mape < bestMape) { bestMape = mape; bestAlpha = alpha; bestBeta = beta; }
            }
        }
        return new double[]{bestAlpha, bestBeta};
    }
 
    private List<Double> applyHolt(List<DataPoint> data, int steps, double alpha, double beta) {
        List<Double> vals = data.stream().map(DataPoint::getValue).collect(Collectors.toList());
        double l = vals.get(0);                   // рівень
        double t = vals.get(1) - vals.get(0);    // тренд
        for (int i = 1; i < vals.size(); i++) {
            double prevL = l;
            l = alpha * vals.get(i) + (1 - alpha) * (l + t);
            t = beta * (l - prevL) + (1 - beta) * t;
        }
        List<Double> preds = new ArrayList<>();
        for (int h = 1; h <= steps; h++) preds.add(l + h * t);
        return preds;
    }
 
    private List<DataPoint> generateForecast(List<DataPoint> history, int horizon, double alpha, double beta) {
        List<Double> vals = history.stream().map(DataPoint::getValue).collect(Collectors.toList());
        double l = vals.get(0), t = vals.get(1) - vals.get(0);
        for (int i = 1; i < vals.size(); i++) {
            double prev = l;
            l = alpha * vals.get(i) + (1 - alpha) * (l + t);
            t = beta * (l - prev) + (1 - beta) * t;
        }
        LocalDate last = history.get(history.size() - 1).getDate();
        List<DataPoint> result = new ArrayList<>();
        for (int h = 1; h <= horizon; h++)
            result.add(new DataPoint(last.plusDays(h), l + h * t));
        return result;
    }
}
