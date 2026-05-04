package com.os.workshop.features.product.supply.create;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Create Supply request payload.")
public record CreateSupplyRequest(
        @Schema(description = "Name.", example = "John Doe") @NotBlank String name,
        @Schema(description = "Sku.", example = "BRK-PAD-001") @NotBlank String sku,
        @Schema(description = "Unit.", example = "UNIT") @NotNull UnitOfMeasure unit,
        @Schema(description = "Category.", example = "Brakes") String category,
        @Schema(description = "Brand.", example = "Toyota") String brand,
        @Schema(description = "Cost Price.", example = "45.00") @NotNull @DecimalMin("0.0") BigDecimal costPrice,
        @Schema(description = "Sale Price.", example = "89.90") @NotNull @DecimalMin("0.0") BigDecimal salePrice,
        @Schema(description = "Fractional Allowed.", example = "true") boolean fractionalAllowed,
        @Schema(description = "Package Size.", example = "1.0") @DecimalMin("0.0") BigDecimal packageSize
) {}
