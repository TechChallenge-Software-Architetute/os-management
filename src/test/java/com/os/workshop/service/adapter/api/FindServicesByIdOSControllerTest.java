package com.os.workshop.service.adapter.api;

import com.os.workshop.features.service.adapter.api.FindServicesByIdOSController;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.FindServicesByIdOSUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindServicesByIdOSControllerTest {

    private final FindServicesByIdOSUC useCase = mock(FindServicesByIdOSUC.class);
    private final FindServicesByIdOSController controller = new FindServicesByIdOSController();

    FindServicesByIdOSControllerTest() {
        ReflectionTestUtils.setField(controller, "findServicesByIdOSUC", useCase);
    }

    @Test
    void returnsServicesByOrderId() {
        UUID id = UUID.randomUUID();

        when(useCase.process(id)).thenReturn(List.of(new ServiceEntity()));

        assertEquals(HttpStatus.OK, controller.findByIdOS(id).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUseCaseFails() {
        UUID id = UUID.randomUUID();

        when(useCase.process(id)).thenThrow(new RuntimeException("failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.findByIdOS(id).getStatusCode());
    }
}
