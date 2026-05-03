package com.os.workshop.service.adapter.api;

import com.os.workshop.features.service.adapter.api.UpdateServiceController;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.UpdateServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateServiceControllerTest {

    private final UpdateServiceUC useCase = mock(UpdateServiceUC.class);
    private final UpdateServiceController controller = new UpdateServiceController(useCase);

    UpdateServiceControllerTest() {
        ReflectionTestUtils.setField(controller, "updateServiceUC", useCase);
    }

    @Test
    void returnsUpdatedService() {
        UUID id = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();

        when(useCase.process(id, request)).thenReturn(new ServiceEntity());

        assertEquals(HttpStatus.OK, controller.update(id, request).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenUseCaseThrowsRuntimeException() {
        UUID id = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();

        when(useCase.process(id, request)).thenThrow(new RuntimeException("not found"));

        assertEquals(HttpStatus.NOT_FOUND, controller.update(id, request).getStatusCode());
    }
}
