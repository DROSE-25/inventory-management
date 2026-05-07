package com.inventory.util;
 
import java.io.*;
import java.time.LocalDate;
import java.util.Random;
 
public class SyntheticDatasetGenerator {
 
    private static final int NUM_PRODUCTS = 50;
    private static final int MONTHS = 12;
    private static final LocalDate START_DATE = LocalDate.of(2025, 1, 1);
    private static final String OUTPUT = "sample_sales.csv";
 
    public static void main(String[] args) throws IOException {
        Random rnd = new Random(42);  // фіксований seed для відтворюваності
 
        try (PrintWriter w = new PrintWriter(new FileWriter(OUTPUT))) {
            w.println("product_sku,warehouse_name,sale_date,quantity,unit_price");
 
            for (int p = 1; p <= NUM_PRODUCTS; p++) {
                String sku = "SKU-" + String.format("%03d", p);
                double basePrice = 20 + rnd.nextDouble() * 80;          // 20–100 грн
                double baseDailyDemand = 5 + rnd.nextDouble() * 20;     // 5–25 шт/день
                double trendPerMonth = (rnd.nextDouble() - 0.4) * 0.5;  // -0.2..+0.3
                double seasonalAmp = rnd.nextDouble() * 0.3;            // 0..30%
                double noise = 0.15;                                    // ±15%
 
                LocalDate date = START_DATE;
                for (int day = 0; day < MONTHS * 30; day++, date = date.plusDays(1)) {
 
                    double month = day / 30.0;
                    double trend = 1 + trendPerMonth * month;
                    double season = 1 + seasonalAmp * Math.sin(
                        2 * Math.PI * month / 12);    // річна синусоїда
                    double shock = 1 + (rnd.nextGaussian() * noise);
 
                    double demand = baseDailyDemand * trend * season * shock;
                    if (demand < 0) demand = 0;
 
                    int qty = (int) Math.round(demand);
                    if (qty == 0) continue;
 
                    w.printf("%s,Головний склад,%s,%d,%.2f%n",
                        sku, date, qty, basePrice);
                }
            }
        }
        System.out.println("Готово: " + OUTPUT);
    }
}
