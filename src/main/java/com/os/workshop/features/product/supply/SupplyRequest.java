package com.os.workshop.features.product.supply;

import com.os.workshop.features.product.domain.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload used to create or update a consumable supply.")
public record SupplyRequest(
        @Schema(description = "Supply name.", example = "Engine oil 5W30")
        @NotBlank String name,

        @Schema(description = "Unique supply SKU.", example = "SUP-OIL-5W30-001")
        @NotBlank String sku,

        @Schema(description = "Unit of measure used to control stock.", example = "LITER")
        @NotNull UnitOfMeasure unit,

        @Schema(description = "Supply category.", example = "Lubricants")
        String category,

        @Schema(description = "Supply brand.", example = "Mobil")
        String brand,

        @Schema(description = "Supply acquisition cost.", example = "32.50")
        @NotNull @DecimalMin("0.0") BigDecimal costPrice,

        @Schema(description = "Supply sale price charged to the customer.", example = "59.90")
        @NotNull @DecimalMin("0.0") BigDecimal salePrice,

        @Schema(description = "Whether fractional quantities are allowed.", example = "true")
        boolean fractionalAllowed,

        @Schema(description = "Package size used for this supply.", example = "1.00")
        @DecimalMin("0.0") BigDecimal packageSize
) {}
