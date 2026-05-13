package com.inventory.forecast;

import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.methods.ForecastingMethod;
import com.inventory.forecast.model.ForecastResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastService {

    // Spring автоматично знаходить всі @Component що реалізують ForecastingMethod
    private final List<ForecastingMethod> methods;

    private Map<String, ForecastingMethod> getMethodMap() {
        return methods.stream()
            .collect(Collectors.toMap(ForecastingMethod::getMethodName, Function.identity()));
    }

    public ForecastResult runForecast(String methodName,
                                     List<DataPoint> history,
                                     int horizon) {
        Map<String, ForecastingMethod> methodMap = getMethodMap();
        if (!methodMap.containsKey(methodName)) {
            throw new IllegalArgumentException("Unknown method: " + methodName +
                ". Available: " + methodMap.keySet());
        }
        log.info("Running forecast: method={}, horizon={}", methodName, horizon);
        return methodMap.get(methodName).forecast(history, horizon);
    }

    /**
     * Запускає всі доступні методи на переданій history,
     * повертає результат з найменшим MAPE.
     */
    public ForecastResult getBestForecast(List<DataPoint> history, int horizon) {
        return methods.stream()
            .map(method -> {
                try {
                    return method.forecast(history, horizon);
                } catch (Exception e) {
                    log.warn("Method {} failed: {}", method.getMethodName(), e.getMessage());
                    return null;
                }
            })
            .filter(result -> result != null && !Double.isNaN(result.getMape()))
            .min(Comparator.comparingDouble(ForecastResult::getMape))
            .orElseThrow(() -> new IllegalStateException(
                "No forecast method produced a valid result for the given history"));
    }

    public List<String> getAvailableMethods() {
        return methods.stream()
            .map(ForecastingMethod::getMethodName)
            .collect(Collectors.toList());
    }
}