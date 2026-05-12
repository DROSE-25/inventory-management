package com.inventory.forecast.model;
 
import com.inventory.forecast.dto.DataPoint;
import lombok.Builder;
import lombok.Data;
import java.util.List;
 
@Data
@Builder
public class ForecastResult {
    private String method;          // назва методу
    private List<DataPoint> forecast; // прогнозні значення
    private double mae;              // Mean Absolute Error
    private double mape;             // Mean Absolute % Error
    private double rmse;             // Root Mean Square Error
    private String description;      // опис для UI
}
