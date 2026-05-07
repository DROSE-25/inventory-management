package com.inventory.exception;
 
import java.math.BigDecimal;
 
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, BigDecimal available, BigDecimal requested) {
        super(String.format(
            "Недостатньо товару (id=%d) на складі: доступно %s, потрібно %s",
            productId, available, requested));
    }
}
