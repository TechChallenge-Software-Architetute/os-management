package com.os.workshop.serviceorder.adapter.api;

import com.os.workshop.serviceorder.domain.CreateOrderRequest;
import com.os.workshop.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.serviceorder.usecases.CreateOrderUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateOSControllerTest {

    private final CreateOrderUC useCase = mock(CreateOrderUC.class);
    private final CreateOSController controller = new CreateOSController();

    CreateOSControllerTest() {
        ReflectionTestUtils.setField(controller, "createOrderUC", useCase);
    }

    @Test
    void returnsCreatedOrder() {
        CreateOrderRequest request = new CreateOrderRequest();

        when(useCase.process(request)).thenReturn(new ServiceOrderEntity());

        assertEquals(HttpStatus.CREATED, controller.createService(request).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUseCaseFails() {
        CreateOrderRequest request = new CreateOrderRequest();

        when(useCase.process(request)).thenThrow(new RuntimeException("failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createService(request).getStatusCode());
    }
}
