package com.os.workshop.features.budget.recalculate;

import com.os.workshop.features.stock.reservation.ReservationChangedEvent;
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

    private final RecalculateBudgetHandler recalculateBudgetHandler;

    @EventListener
    public void onReservationChanged(ReservationChangedEvent event) {
        recalculateBudgetHandler.handle(event.serviceOrderId());
    }
}
