package com.inventory.forecast.dto;

import com.inventory.forecast.model.ForecastResult;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CompareResponse {
    private Long   productId;
    private String productName;
    private int    horizon;
    private List<ForecastResult> results; // всі методи
    private ForecastResult       best;    // найкращий за MAPE
}

