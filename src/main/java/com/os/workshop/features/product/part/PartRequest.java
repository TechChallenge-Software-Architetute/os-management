package com.os.workshop.features.product.part;

import com.os.workshop.features.product.domain.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload used to create or update an automotive part.")
public record PartRequest(
        @Schema(description = "Part name.", example = "Oil filter")
        @NotBlank String name,

        @Schema(description = "Unique part SKU.", example = "PART-OIL-FILTER-001")
        @NotBlank String sku,

        @Schema(description = "Unit of measure used to control stock.", example = "UNIT")
        @NotNull UnitOfMeasure unit,

        @Schema(description = "Part category.", example = "Filters")
        String category,

        @Schema(description = "Part brand.", example = "Bosch")
        String brand,

        @Schema(description = "Part acquisition cost.", example = "25.90")
        @NotNull @DecimalMin("0.0") BigDecimal costPrice,

        @Schema(description = "Part sale price charged to the customer.", example = "49.90")
        @NotNull @DecimalMin("0.0") BigDecimal salePrice,

        @Schema(description = "Manufacturer reference code.", example = "OF-7788")
        String manufacturerCode,

        @Schema(description = "Warranty period in months.", example = "12")
        @Min(0) int warrantyMonths
) {}
