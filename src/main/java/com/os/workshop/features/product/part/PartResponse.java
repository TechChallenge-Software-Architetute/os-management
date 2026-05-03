package com.os.workshop.features.product.part;

import com.os.workshop.features.product.domain.Part;
import com.os.workshop.features.product.domain.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Automotive part data returned by the API.")
public record PartResponse(
        @Schema(description = "Part unique identifier.", example = "1")
        Long id,
        @Schema(description = "Part name.", example = "Oil filter")
        String name,
        @Schema(description = "Unique part SKU.", example = "PART-OIL-FILTER-001")
        String sku,
        @Schema(description = "Unit of measure used to control stock.", example = "UNIT")
        UnitOfMeasure unit,
        @Schema(description = "Part category.", example = "Filters")
        String category,
        @Schema(description = "Part brand.", example = "Bosch")
        String brand,
        @Schema(description = "Part acquisition cost.", example = "25.90")
        BigDecimal costPrice,
        @Schema(description = "Part sale price charged to the customer.", example = "49.90")
        BigDecimal salePrice,
        @Schema(description = "Whether the part is active.", example = "true")
        boolean active,
        @Schema(description = "Manufacturer reference code.", example = "OF-7788")
        String manufacturerCode,
        @Schema(description = "Warranty period in months.", example = "12")
        int warrantyMonths,
        @Schema(description = "Record creation date and time.", example = "2026-05-03T12:30:00")
        LocalDateTime createdAt,
        @Schema(description = "Last update date and time.", example = "2026-05-03T12:45:00")
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
