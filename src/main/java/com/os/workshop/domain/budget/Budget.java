package com.os.workshop.domain.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public void recalculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(BudgetItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
