package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.service.adapter.api.CreateServiceController;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.CreateServiceRequest;
import com.os.workshop.features.service.usecases.CreateServiceUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateServiceControllerTest {

    private final CreateServiceUC useCase = mock(CreateServiceUC.class);
    private final CreateServiceController controller = new CreateServiceController();

    CreateServiceControllerTest() {
        ReflectionTestUtils.setField(controller, "createServiceUC", useCase);
    }

    @Test
    void returnsCreatedWhenServiceIsCreated() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setServiceType("REVISAO");
        request.setIdOS(UUID.randomUUID());

        when(useCase.process(request)).thenReturn(new ServiceEntity());

        assertEquals(HttpStatus.CREATED, controller.createService(request).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUseCaseFails() {
        CreateServiceRequest request = new CreateServiceRequest();

        when(useCase.process(request)).thenThrow(new RuntimeException("failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createService(request).getStatusCode());
    }
}
