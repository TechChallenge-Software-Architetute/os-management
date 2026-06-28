package com.os.workshop.features.budget.shared.repository;

import com.os.workshop.domain.product.ProductType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "budget_items")
public class BudgetItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "budget_item_seq")
    @SequenceGenerator(name = "budget_item_seq", sequenceName = "budget_item_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long budgetId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String productSku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalPrice;
}
