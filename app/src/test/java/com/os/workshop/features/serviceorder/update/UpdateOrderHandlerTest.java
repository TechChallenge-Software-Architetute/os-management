package com.os.workshop.features.serviceorder.update;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderHandlerTest {

    private final ServiceOrderJpaRepository serviceOrderJpaRepository = mock(ServiceOrderJpaRepository.class);
    private final FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler = mock(FindBudgetByServiceOrderHandler.class);
    private final UpdateOrderHandler handler = new UpdateOrderHandler();

    UpdateOrderHandlerTest() {
        ReflectionTestUtils.setField(handler, "serviceOrderJpaRepository", serviceOrderJpaRepository);
        ReflectionTestUtils.setField(handler, "findBudgetByServiceOrderHandler", findBudgetByServiceOrderHandler);
    }

    @Test
    void updatesOrderStatus() {
        UUID id = UUID.randomUUID();
        ServiceOrderEntity entity = new ServiceOrderEntity();
        entity.setId(id); entity.setServiceTypeName("[TROCA]"); entity.setServiceStatus("RECEBIDA");
        when(serviceOrderJpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.EM_DIAGNOSTICO);
        var result = handler.handle(id, request);

        assertEquals(OrderServiceStatusEnum.EM_DIAGNOSTICO.getStatus(), result.getServiceStatus());
    }

    @Test
    void throwsWhenOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(serviceOrderJpaRepository.findById(id)).thenReturn(Optional.empty());
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.FINALIZADA);
        assertThrows(NoSuchElementException.class, () -> handler.handle(id, request));
    }

    @Test
    void throwsWhenRequestIsNull() {
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> handler.handle(id, null));
    }

    @Test
    void throwsWhenStatusIsNull() {
        UUID id = UUID.randomUUID();
        var result = new UpdateOrderRequest();
        assertThrows(IllegalArgumentException.class, () -> handler.handle(id, result));
    }
}
