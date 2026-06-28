package com.os.workshop.adapter.in.web.product.supply;

import com.os.workshop.domain.product.Supply;
import com.os.workshop.domain.product.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SupplyResponse(
        Long id,
        String name,
        String sku,
        UnitOfMeasure unit,
        String category,
        String brand,
        BigDecimal costPrice,
        BigDecimal salePrice,
        boolean active,
        boolean fractionalAllowed,
        BigDecimal packageSize,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SupplyResponse from(Supply supply) {
        return new SupplyResponse(
                supply.getId(), supply.getName(), supply.getSku(), supply.getUnit(),
                supply.getCategory(), supply.getBrand(), supply.getCostPrice(), supply.getSalePrice(),
                supply.isActive(), supply.isFractionalAllowed(), supply.getPackageSize(),
                supply.getCreatedAt(), supply.getUpdatedAt()
        );
    }
}
