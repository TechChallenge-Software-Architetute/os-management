package com.os.workshop.application.serviceorder;

import com.os.workshop.application.notification.OrderStatusNotificationService;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UpdateOrderManualCancelTest {

    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private OrderStatusNotificationService notificationService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ServiceOrderMetrics metrics;

    @InjectMocks private UpdateOrderUseCase useCase;

    @Test
    void cancelamentoManualEBloqueado() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), OrderServiceStatusEnum.CANCELADA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("saga");

        verifyNoInteractions(serviceOrderRepository, notificationService, eventPublisher);
        verify(metrics).processingFailed("update_status");
    }
}
