package com.os.workshop.application.budget;

import com.os.workshop.application.saga.OrderSagaEvent;
import com.os.workshop.domain.stock.ReservationChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BudgetEventListener {

    private final RecalculateBudgetUseCase recalculateBudgetUseCase;
    private final FindBudgetByServiceOrderUseCase findBudgetByServiceOrderUseCase;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onReservationChanged(ReservationChangedEvent event) {
        boolean firstBudget = findBudgetByServiceOrderUseCase.execute(event.serviceOrderId()).isEmpty();
        var budget = recalculateBudgetUseCase.execute(event.serviceOrderId());
        if (firstBudget && budget.getTotalPrice() != null && budget.getTotalPrice().signum() > 0) {
            eventPublisher.publishEvent(OrderSagaEvent.created(event.serviceOrderId(), "ORCAMENTO_GERADO",
                    budget.getTotalPrice(), UUID.randomUUID().toString()));
        }
    }
}
