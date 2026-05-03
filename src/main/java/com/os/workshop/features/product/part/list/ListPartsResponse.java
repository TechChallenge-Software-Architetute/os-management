package com.os.workshop.features.product.part.list;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListPartsResponse(
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
    public static ListPartsResponse from(Part part) {
        return new ListPartsResponse(
                part.getId(), part.getName(), part.getSku(), part.getUnit(),
                part.getCategory(), part.getBrand(), part.getCostPrice(), part.getSalePrice(),
                part.isActive(), part.getManufacturerCode(), part.getWarrantyMonths(),
                part.getCreatedAt(), part.getUpdatedAt()
        );
    }
}
