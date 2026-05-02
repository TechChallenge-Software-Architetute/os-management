package com.os.workshop.budget;

import com.os.workshop.stock.reservation.ReservationChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for reservation changes and triggers budget recalculation.
 * This keeps the budget module decoupled from the reservation module.
 */
@Component
@RequiredArgsConstructor
public class BudgetEventListener {

    private final BudgetService budgetService;

    @EventListener
    public void onReservationChanged(ReservationChangedEvent event) {
        budgetService.recalculate(event.serviceOrderId());
    }
}
