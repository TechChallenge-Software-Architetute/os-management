package com.os.workshop.features.product.part.update;

import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePartRequest(
        @NotBlank String name,
        @NotBlank String sku,
        @NotNull UnitOfMeasure unit,
        String category,
        String brand,
        @NotNull @DecimalMin("0.0") BigDecimal costPrice,
        @NotNull @DecimalMin("0.0") BigDecimal salePrice,
        String manufacturerCode,
        @Min(0) int warrantyMonths
) {}
