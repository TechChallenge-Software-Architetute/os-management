package com.os.workshop.features.product.supply;

import com.os.workshop.features.product.domain.Supply;
import com.os.workshop.features.product.domain.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Consumable supply data returned by the API.")
public record SupplyResponse(
        @Schema(description = "Supply unique identifier.", example = "1")
        Long id,
        @Schema(description = "Supply name.", example = "Engine oil 5W30")
        String name,
        @Schema(description = "Unique supply SKU.", example = "SUP-OIL-5W30-001")
        String sku,
        @Schema(description = "Unit of measure used to control stock.", example = "LITER")
        UnitOfMeasure unit,
        @Schema(description = "Supply category.", example = "Lubricants")
        String category,
        @Schema(description = "Supply brand.", example = "Mobil")
        String brand,
        @Schema(description = "Supply acquisition cost.", example = "32.50")
        BigDecimal costPrice,
        @Schema(description = "Supply sale price charged to the customer.", example = "59.90")
        BigDecimal salePrice,
        @Schema(description = "Whether the supply is active.", example = "true")
        boolean active,
        @Schema(description = "Whether fractional quantities are allowed.", example = "true")
        boolean fractionalAllowed,
        @Schema(description = "Package size used for this supply.", example = "1.00")
        BigDecimal packageSize,
        @Schema(description = "Record creation date and time.", example = "2026-05-03T12:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update date and time.", example = "2026-05-03T12:45:00")
        LocalDateTime updatedAt
) {
    public static SupplyResponse from(Supply supply) {
        return new SupplyResponse(
                supply.getId(),
                supply.getName(),
                supply.getSku(),
                supply.getUnit(),
                supply.getCategory(),
                supply.getBrand(),
                supply.getCostPrice(),
                supply.getSalePrice(),
                supply.isActive(),
                supply.isFractionalAllowed(),
                supply.getPackageSize(),
                supply.getCreatedAt(),
                supply.getUpdatedAt()
        );
    }
}
