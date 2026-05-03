package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.service.adapter.api.ListServiceTypeController;
import com.os.workshop.features.service.domain.ServiceTypeEntity;
import com.os.workshop.features.service.usecases.ListServiceTypeUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListServiceTypeControllerTest {

    private final ListServiceTypeUC useCase = mock(ListServiceTypeUC.class);
    private final ListServiceTypeController controller = new ListServiceTypeController();

    ListServiceTypeControllerTest() {
        ReflectionTestUtils.setField(controller, "listServiceTypeUC", useCase);
    }

    @Test
    void returnsServiceTypes() {
        when(useCase.process()).thenReturn(List.of(new ServiceTypeEntity(UUID.randomUUID(), "REVISAO", "Revisao")));

        assertEquals(HttpStatus.OK, controller.listServices().getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUseCaseFails() {
        when(useCase.process()).thenThrow(new RuntimeException("failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.listServices().getStatusCode());
    }
}
