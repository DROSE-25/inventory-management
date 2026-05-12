package com.inventory.forecast.evaluation;
 
import com.inventory.forecast.dto.DataPoint;
import org.springframework.stereotype.Component;
import java.util.List;
 
@Component
public class ForecastAccuracyEvaluator {
 
    public double mae(List<Double> actual, List<Double> predicted) {
        double sum = 0;
        for (int i = 0; i < actual.size(); i++)
            sum += Math.abs(actual.get(i) - predicted.get(i));
        return sum / actual.size();
    }
 
    public double mape(List<Double> actual, List<Double> predicted) {
        double sum = 0;
        for (int i = 0; i < actual.size(); i++)
            if (actual.get(i) != 0)
                sum += Math.abs((actual.get(i) - predicted.get(i)) / actual.get(i));
        return (sum / actual.size()) * 100;
    }
 
    public double rmse(List<Double> actual, List<Double> predicted) {
        double sum = 0;
        for (int i = 0; i < actual.size(); i++) {
            double diff = actual.get(i) - predicted.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum / actual.size());
    }
}
