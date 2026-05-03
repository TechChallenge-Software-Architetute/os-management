package com.os.workshop.features.budget.shared.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    private Long id;
    private UUID serviceOrderId;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private List<BudgetItem> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Recalculates the total price based on all budget items.
     */
    public void recalculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(BudgetItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
