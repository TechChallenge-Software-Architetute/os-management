package com.os.workshop.adapter.in.web.product.supply;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.domain.product.Supply;
import com.os.workshop.domain.product.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "List Supplies response payload.")
public record SupplyResponse(
        @Schema(description = "Identifier.", example = "1") Long id,
        @Schema(description = "Name.", example = "John Doe") String name,
        @Schema(description = "Sku.", example = "BRK-PAD-001") String sku,
        @Schema(description = "Unit.", example = "UNIT") UnitOfMeasure unit,
        @Schema(description = "Category.", example = "Brakes") String category,
        @Schema(description = "Brand.", example = "Toyota") String brand,
        @Schema(description = "Cost Price.", example = "45.00") BigDecimal costPrice,
        @Schema(description = "Sale Price.", example = "89.90") BigDecimal salePrice,
        @Schema(description = "Active.", example = "true") boolean active,
        @Schema(description = "Fractional Allowed.", example = "true") boolean fractionalAllowed,
        @Schema(description = "Package Size.", example = "1.0") BigDecimal packageSize,
        @Schema(description = "Created At.", example = "2026-05-03T10:00:00") LocalDateTime createdAt,
        @Schema(description = "Updated At.", example = "2026-05-03T10:00:00") LocalDateTime updatedAt
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
