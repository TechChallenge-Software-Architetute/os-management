package com.os.workshop.application.budget;

import com.os.workshop.application.budget.port.out.BudgetRepository;
import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.application.saga.SagaCorrelation;
import com.os.workshop.application.saga.SagaMessage;
import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;
import com.os.workshop.domain.stock.ReservationChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BudgetEventListener {

    private final RecalculateBudgetUseCase recalculateBudgetUseCase;
    private final SagaCommandPublisherPort sagaCommandPublisher;
    private final BudgetRepository budgetRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onReservationChanged(ReservationChangedEvent event) {
        var previousAmount = budgetRepository.findByServiceOrderId(event.serviceOrderId())
                .map(budget -> budget.getTotalPrice())
                .orElse(null);
        var budget = recalculateBudgetUseCase.execute(event.serviceOrderId());
        boolean amountBecameAvailable = budget.getTotalPrice() != null
                && budget.getTotalPrice().signum() > 0
                && (previousAmount == null || previousAmount.signum() <= 0);
        if (amountBecameAvailable) {
            sagaCommandPublisher.publish(new SagaMessage(
                    event.serviceOrderId(), SagaAction.ORCAMENTO_DISPONIVEL,
                    budget.getTotalPrice(), SagaCorrelation.current()));
        }
    }
}
