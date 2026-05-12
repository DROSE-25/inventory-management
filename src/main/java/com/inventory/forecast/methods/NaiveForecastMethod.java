package com.inventory.forecast.methods;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.model.ForecastResult;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
@Component  // Spring знайде цей клас автоматично
public class NaiveForecastMethod implements ForecastingMethod {
 
    @Override
    public String getMethodName() { return "NAIVE"; }
 
    @Override
    public ForecastResult forecast(List<DataPoint> history, int horizon) {
        double lastValue = history.get(history.size() - 1).getValue();
        LocalDate lastDate = history.get(history.size() - 1).getDate();
 
        List<DataPoint> result = new ArrayList<>();
        for (int i = 1; i <= horizon; i++) {
            result.add(new DataPoint(lastDate.plusDays(i), lastValue));
        }
 
        return ForecastResult.builder()
            .method("NAIVE")
            .forecast(result)
            .mae(0.0).mape(0.0).rmse(0.0) // заглушка
            .description("Naive: repeats last observed value")
            .build();
    }
}
