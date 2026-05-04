package com.os.workshop.features.product.part.findById;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Find Part By Id response payload.")
public record FindPartByIdResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Name.", example = "John Doe") String name,
        @Schema(description = "Sku.", example = "BRK-PAD-001") String sku,
        @Schema(description = "Unit.", example = "UNIT") UnitOfMeasure unit,
        @Schema(description = "Category.", example = "Brakes") String category,
        @Schema(description = "Brand.", example = "Toyota") String brand,
        @Schema(description = "Cost Price.", example = "45.00") BigDecimal costPrice,
        @Schema(description = "Sale Price.", example = "89.90") BigDecimal salePrice,
        @Schema(description = "Active.", example = "true") boolean active,
        @Schema(description = "Manufacturer Code.", example = "BOH-BP-2025") String manufacturerCode,
        @Schema(description = "Warranty Months.", example = "12") int warrantyMonths,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
) {
    public static FindPartByIdResponse from(Part part) {
        return new FindPartByIdResponse(
                part.getId(), part.getName(), part.getSku(), part.getUnit(),
                part.getCategory(), part.getBrand(), part.getCostPrice(), part.getSalePrice(),
                part.isActive(), part.getManufacturerCode(), part.getWarrantyMonths(),
                part.getCreatedAt(), part.getUpdatedAt()
        );
    }
}
