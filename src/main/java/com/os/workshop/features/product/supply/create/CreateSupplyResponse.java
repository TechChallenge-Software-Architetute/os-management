package com.os.workshop.features.product.supply.create;

import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateSupplyResponse(
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
    public static CreateSupplyResponse from(Supply supply) {
        return new CreateSupplyResponse(
                supply.getId(), supply.getName(), supply.getSku(), supply.getUnit(),
                supply.getCategory(), supply.getBrand(), supply.getCostPrice(), supply.getSalePrice(),
                supply.isActive(), supply.isFractionalAllowed(), supply.getPackageSize(),
                supply.getCreatedAt(), supply.getUpdatedAt()
        );
    }
}
