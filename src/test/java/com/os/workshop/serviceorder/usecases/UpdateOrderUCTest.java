package com.os.workshop.serviceorder.usecases;

import com.os.workshop.features.serviceorder.adapter.database.OrderRepository;
import com.os.workshop.features.serviceorder.domain.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.domain.UpdateOrderRequest;
import com.os.workshop.features.serviceorder.usecases.UpdateOrderUC;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateOrderUCTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final UpdateOrderUC useCase = new UpdateOrderUC();

    UpdateOrderUCTest() {
        ReflectionTestUtils.setField(useCase, "orderRepository", orderRepository);
    }

    @Test
    void updatesOrderStatus() {
        UUID id = UUID.randomUUID();
        ServiceOrderEntity order = new ServiceOrderEntity();
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.APROVADO);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        assertEquals(OrderServiceStatusEnum.APROVADO.getStatus(), useCase.process(id, request).getServiceStatus());
    }

    @Test
    void validatesRequestStatus() {
        UUID id = UUID.randomUUID();
        UpdateOrderRequest request = new UpdateOrderRequest();

        assertThrows(IllegalArgumentException.class, () -> useCase.process(id, request));
    }

    @Test
    void throwsWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.APROVADO);

        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> useCase.process(id, request));
    }
}
