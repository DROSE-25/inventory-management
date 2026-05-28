package com.inventory.forecast.methods;

import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.model.ForecastResult;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class NaiveForecastMethod implements ForecastingMethod {

    @Override
    public String getMethodName() { return "NAIVE"; }

    @Override
    public ForecastResult forecast(List<DataPoint> history, int horizon) {
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("History is empty");
        }

        double lastValue = history.get(history.size() - 1).getValue();
        LocalDate lastDate = history.get(history.size() - 1).getDate();

        // Прогноз: повторюємо останнє значення
        List<DataPoint> result = new ArrayList<>();
        for (int i = 1; i <= horizon; i++) {
            result.add(new DataPoint(lastDate.plusDays(i), lastValue));
        }

        // Рахуємо реальні метрики на тренувальних даних (walk-forward)
        // Naive: прогноз для t+1 = значення t
        double mae = 0.0, mape = 0.0, rmse = 0.0;
        int n = history.size();
        int count = 0;

        if (n >= 2) {
            for (int i = 1; i < n; i++) {
                double actual = history.get(i).getValue();
                double predicted = history.get(i - 1).getValue();
                double error = Math.abs(actual - predicted);
                mae += error;
                rmse += error * error;
                if (actual != 0) {
                    mape += (error / Math.abs(actual)) * 100.0;
                }
                count++;
            }
            mae  = mae  / count;
            rmse = Math.sqrt(rmse / count);
            mape = mape / count;
        }

        return ForecastResult.builder()
            .method("NAIVE")
            .forecast(result)
            .mae(mae)
            .mape(mape)
            .rmse(rmse)
            .description("Naive: repeats last observed value")
            .build();
    }
}
