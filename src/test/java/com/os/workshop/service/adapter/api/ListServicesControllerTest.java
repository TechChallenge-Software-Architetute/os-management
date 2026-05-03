package com.os.workshop.service.adapter.api;

import com.os.workshop.features.service.adapter.api.ListServicesController;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.ListServicesUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListServicesControllerTest {

    private final ListServicesUC useCase = mock(ListServicesUC.class);
    private final ListServicesController controller = new ListServicesController();

    ListServicesControllerTest() {
        ReflectionTestUtils.setField(controller, "listServicesUC", useCase);
    }

    @Test
    void returnsServices() {
        when(useCase.process()).thenReturn(List.of(new ServiceEntity()));

        assertEquals(HttpStatus.OK, controller.listServices().getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUseCaseFails() {
        when(useCase.process()).thenThrow(new RuntimeException("failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.listServices().getStatusCode());
    }
}
