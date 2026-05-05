package com.os.workshop.features.product.part.create;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Create Part request payload.")
public record CreatePartRequest(
        @Schema(description = "Name.", example = "John Doe") @NotBlank String name,
        @Schema(description = "Sku.", example = "BRK-PAD-001")
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9]+[\\-_][A-Za-z0-9\\-_]+$", message = "SKU deve seguir o formato prefixo-sufixo (ex: BRK-PAD-001)")
        String sku,
        @Schema(description = "Unit.", example = "UNIT") @NotNull UnitOfMeasure unit,
        @Schema(description = "Category.", example = "Brakes") String category,
        @Schema(description = "Brand.", example = "Toyota") String brand,
        @Schema(description = "Cost Price.", example = "45.00") @NotNull @DecimalMin("0.0") BigDecimal costPrice,
        @Schema(description = "Sale Price.", example = "89.90") @NotNull @DecimalMin("0.0") BigDecimal salePrice,
        @Schema(description = "Manufacturer Code.", example = "BOH-BP-2025") String manufacturerCode,
        @Schema(description = "Warranty Months.", example = "12") @Min(0) int warrantyMonths
) {}
