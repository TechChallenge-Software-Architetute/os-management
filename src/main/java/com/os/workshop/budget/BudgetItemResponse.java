package com.os.workshop.budget;

import com.os.workshop.budget.domain.BudgetItem;
import com.os.workshop.product.domain.ProductType;

import java.math.BigDecimal;

public record BudgetItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        ProductType productType,
        BigDecimal quantity,
        BigDecimal unitPrice,
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
