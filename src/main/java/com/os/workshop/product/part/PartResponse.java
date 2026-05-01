package com.os.workshop.product.part;

import com.os.workshop.product.domain.Part;
import com.os.workshop.product.domain.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PartResponse(
        Long id,
        String name,
        String sku,
        UnitOfMeasure unit,
        String category,
        String brand,
        BigDecimal costPrice,
        BigDecimal salePrice,
        boolean active,
        String manufacturerCode,
        int warrantyMonths,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PartResponse from(Part part) {
        return new PartResponse(
                part.getId(),
                part.getName(),
                part.getSku(),
                part.getUnit(),
                part.getCategory(),
                part.getBrand(),
                part.getCostPrice(),
                part.getSalePrice(),
                part.isActive(),
                part.getManufacturerCode(),
                part.getWarrantyMonths(),
                part.getCreatedAt(),
                part.getUpdatedAt()
        );
    }
}
