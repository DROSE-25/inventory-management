package com.inventory.forecast.methods;
 
import com.inventory.forecast.dto.DataPoint;
import com.inventory.forecast.model.ForecastResult;
import java.util.List;
 
public interface ForecastingMethod {
 
    // Унікальна назва методу (використовується як ідентифікатор)
    String getMethodName();
 
    // Головний метод: приймає історію, повертає прогноз на horizon кроків
    ForecastResult forecast(List<DataPoint> history, int horizon);
}
