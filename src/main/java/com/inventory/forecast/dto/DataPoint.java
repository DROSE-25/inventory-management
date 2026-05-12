package com.inventory.forecast.dto;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataPoint {
    private LocalDate date;
    private double value;
}
