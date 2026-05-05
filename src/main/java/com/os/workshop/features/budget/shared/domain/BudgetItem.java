package com.os.workshop.features.budget.shared.domain;

import com.os.workshop.features.product.shared.domain.ProductType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItem {

    private Long id;
    private Long budgetId;
    private Long productId;
    private String productName;
    private String productSku;
    private ProductType productType;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    /**
     * Creates a budget item snapshot from product data and reservation quantity.
     */
    public static BudgetItem snapshot(Long productId, String name, String sku,
                                       ProductType type, BigDecimal salePrice, BigDecimal quantity) {
        BudgetItem item = new BudgetItem();
        item.setProductId(productId);
        item.setProductName(name);
        item.setProductSku(sku);
        item.setProductType(type);
        item.setQuantity(quantity);
        item.setUnitPrice(salePrice);
        item.setTotalPrice(salePrice.multiply(quantity));
        return item;
    }
}
