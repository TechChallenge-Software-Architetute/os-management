package com.os.workshop.serviceorder.adapter.api;

import com.os.workshop.features.serviceorder.adapter.api.ConsultOrderController;
import com.os.workshop.features.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.usecases.ConsultOrderUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultOrderControllerTest {

    private final ConsultOrderUC useCase = mock(ConsultOrderUC.class);
    private final ConsultOrderController controller = new ConsultOrderController();

    ConsultOrderControllerTest() {
        ReflectionTestUtils.setField(controller, "consultOrderUC", useCase);
    }

    @Test
    void returnsOrders() {
        when(useCase.process()).thenReturn(List.of(new ServiceOrderEntity()));

        assertEquals(HttpStatus.OK, controller.consultOrders().getStatusCode());
    }

    @Test
    void returnsOrderById() {
        UUID id = UUID.randomUUID();

        when(useCase.process(id)).thenReturn(new ServiceOrderEntity());

        assertEquals(HttpStatus.OK, controller.consultOrderById(id).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenRuntimeExceptionOccurs() {
        UUID id = UUID.randomUUID();

        when(useCase.process(id)).thenThrow(new RuntimeException("not found"));

        assertEquals(HttpStatus.NOT_FOUND, controller.consultOrderById(id).getStatusCode());
    }
}
