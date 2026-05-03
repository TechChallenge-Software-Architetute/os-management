package com.os.workshop.serviceorder.adapter.api;

import com.os.workshop.features.serviceorder.adapter.api.UpdateOSController;
import com.os.workshop.features.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.domain.UpdateOrderRequest;
import com.os.workshop.features.serviceorder.usecases.UpdateOrderUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateOSControllerTest {

    private final UpdateOrderUC useCase = mock(UpdateOrderUC.class);
    private final UpdateOSController controller = new UpdateOSController(useCase);

    UpdateOSControllerTest() {
        ReflectionTestUtils.setField(controller, "updateOrderUC", useCase);
    }

    @Test
    void returnsUpdatedOrder() {
        UUID id = UUID.randomUUID();
        UpdateOrderRequest request = new UpdateOrderRequest();

        when(useCase.process(id, request)).thenReturn(new ServiceOrderEntity());

        assertEquals(HttpStatus.OK, controller.updateOrder(id, request).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdateOrderRequest request = new UpdateOrderRequest();

        when(useCase.process(id, request)).thenThrow(new NoSuchElementException());

        assertEquals(HttpStatus.NOT_FOUND, controller.updateOrder(id, request).getStatusCode());
    }

    @Test
    void returnsBadRequestWhenRequestIsInvalid() {
        UUID id = UUID.randomUUID();
        UpdateOrderRequest request = new UpdateOrderRequest();

        when(useCase.process(id, request)).thenThrow(new IllegalArgumentException());

        assertEquals(HttpStatus.BAD_REQUEST, controller.updateOrder(id, request).getStatusCode());
    }
}
