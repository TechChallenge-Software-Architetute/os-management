package com.os.workshop.service.adapter.api;

import com.os.workshop.features.service.adapter.api.FindServiceByIdController;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.FindServiceByIdUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindServiceByIdControllerTest {

    private final FindServiceByIdUC useCase = mock(FindServiceByIdUC.class);
    private final FindServiceByIdController controller = new FindServiceByIdController();

    FindServiceByIdControllerTest() {
        ReflectionTestUtils.setField(controller, "findServiceByIdUC", useCase);
    }

    @Test
    void returnsServiceWhenFound() {
        UUID id = UUID.randomUUID();

        when(useCase.process(id)).thenReturn(new ServiceEntity());

        assertEquals(HttpStatus.OK, controller.findById(id).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenUseCaseThrowsRuntimeException() {
        UUID id = UUID.randomUUID();

        when(useCase.process(id)).thenThrow(new RuntimeException("not found"));

        assertEquals(HttpStatus.NOT_FOUND, controller.findById(id).getStatusCode());
    }
}
