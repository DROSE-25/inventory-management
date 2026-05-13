package com.inventory.forecast.methods;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.evaluation.ForecastAccuracyEvaluator;
import com.inventory.forecast.model.ForecastResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
 
@Component
@RequiredArgsConstructor
public class LinearRegressionMethod implements ForecastingMethod {
 
    private final ForecastAccuracyEvaluator evaluator;
 
    @Override
    public String getMethodName() { return "LINEAR_REGRESSION"; }
 
    @Override
    public ForecastResult forecast(List<DataPoint> history, int horizon) {
        if (history.size() < 3)
            throw new IllegalArgumentException("Linear Regression requires at least 3 data points");
 
        // Навчаємо регресію на всіх даних
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < history.size(); i++)
            regression.addData(i + 1, history.get(i).getValue());
 
        // Метрики: прогноз на тренувальних точках vs реальні значення
        int split = (int)(history.size() * 0.8);
        SimpleRegression regTrain = new SimpleRegression();
        List<DataPoint> train = history.subList(0, split);
        for (int i = 0; i < train.size(); i++)
            regTrain.addData(i + 1, train.get(i).getValue());
 
        List<Double> actual = history.subList(split, history.size())
            .stream().map(DataPoint::getValue).collect(Collectors.toList());
        List<Double> predicted = new ArrayList<>();
        for (int i = split + 1; i <= history.size(); i++)
            predicted.add(regTrain.predict(i));
 
        double mae  = actual.isEmpty() ? 0 : evaluator.mae(actual, predicted);
        double mape = actual.isEmpty() ? 0 : evaluator.mape(actual, predicted);
        double rmse = actual.isEmpty() ? 0 : evaluator.rmse(actual, predicted);
 
        // Генеруємо прогноз на horizon кроків вперед
        LocalDate last = history.get(history.size() - 1).getDate();
        List<DataPoint> forecastPoints = new ArrayList<>();
        for (int h = 1; h <= horizon; h++)
            forecastPoints.add(new DataPoint(last.plusDays(h), regression.predict(history.size() + h)));
 
        return ForecastResult.builder()
            .method("LINEAR_REGRESSION")
            .forecast(forecastPoints)
            .mae(mae).mape(mape).rmse(rmse)
            .description(String.format("Linear Regression (slope=%.3f, intercept=%.3f)",
                regression.getSlope(), regression.getIntercept()))
            .build();
    }
}

