package com.inventory.mapper;

import com.inventory.dto.response.SaleResponse;
import com.inventory.model.Sale;

public class SaleMapper {

    public static SaleResponse toDTO(Sale sale) {
        return SaleResponse.builder()
            .id(sale.getId())
            .productId(sale.getProduct().getId())
            .productName(sale.getProduct().getName())
            .warehouseId(sale.getWarehouse().getId())
            .warehouseName(sale.getWarehouse().getName())
            .saleDate(sale.getSaleDate())
            .quantity(sale.getQuantity())
            .unitPrice(sale.getUnitPrice())
            .build();
    }
}