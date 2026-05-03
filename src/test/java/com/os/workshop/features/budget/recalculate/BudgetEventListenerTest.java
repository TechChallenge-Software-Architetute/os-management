package com.os.workshop.features.budget.recalculate;

import com.os.workshop.features.stock.reservation.ReservationChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BudgetEventListenerTest {

    @Mock
    private RecalculateBudgetHandler recalculateBudgetHandler;

    @InjectMocks
    private BudgetEventListener budgetEventListener;

    @Test
    void whenReservationChangedEventReceived_thenBudgetIsRecalculated() {
        UUID osId = UUID.randomUUID();
        ReservationChangedEvent event = new ReservationChangedEvent(osId);

        budgetEventListener.onReservationChanged(event);

        verify(recalculateBudgetHandler).handle(osId);
    }
}
