package com.os.workshop.adapter.in.web.product.supply;

import com.os.workshop.domain.product.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateSupplyRequest(
        @NotBlank String name,
        @NotBlank String sku,
        @NotNull UnitOfMeasure unit,
        String category,
        String brand,
        @NotNull @DecimalMin("0.0") BigDecimal costPrice,
        @NotNull @DecimalMin("0.0") BigDecimal salePrice,
        boolean fractionalAllowed,
        @DecimalMin("0.0") BigDecimal packageSize
) {}
