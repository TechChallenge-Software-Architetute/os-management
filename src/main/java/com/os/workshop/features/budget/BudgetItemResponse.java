package com.os.workshop.features.budget;

import com.os.workshop.features.budget.domain.BudgetItem;
import com.os.workshop.features.product.domain.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Budget line item generated from a reserved product.")
public record BudgetItemResponse(
        @Schema(description = "Budget item unique identifier.", example = "1")
        Long id,
        @Schema(description = "Reserved product identifier.", example = "10")
        Long productId,
        @Schema(description = "Reserved product name.", example = "Oil filter")
        String productName,
        @Schema(description = "Reserved product SKU.", example = "PART-OIL-FILTER-001")
        String productSku,
        @Schema(description = "Reserved product type.", example = "PART")
        ProductType productType,
        @Schema(description = "Reserved quantity.", example = "2.00")
        BigDecimal quantity,
        @Schema(description = "Unit price used for the budget item.", example = "49.90")
        BigDecimal unitPrice,
        @Schema(description = "Total price for this budget item.", example = "99.80")
        BigDecimal totalPrice
) {
    public static BudgetItemResponse from(BudgetItem item) {
        return new BudgetItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductSku(),
                item.getProductType(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}
