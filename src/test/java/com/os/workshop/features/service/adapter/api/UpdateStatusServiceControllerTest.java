package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.service.adapter.api.UpdateStatusServiceController;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.UpdateStatusServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceStatusUC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateStatusServiceControllerTest {

    private final UpdateServiceStatusUC useCase = mock(UpdateServiceStatusUC.class);
    private final UpdateStatusServiceController controller = new UpdateStatusServiceController();

    UpdateStatusServiceControllerTest() {
        ReflectionTestUtils.setField(controller, "updateServiceStatusUC", useCase);
    }

    @Test
    void returnsUpdatedService() {
        UpdateStatusServiceRequest request = new UpdateStatusServiceRequest();

        when(useCase.process(request)).thenReturn(new ServiceEntity());

        assertEquals(HttpStatus.OK, controller.listServices(request).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUseCaseFails() {
        UpdateStatusServiceRequest request = new UpdateStatusServiceRequest();

        when(useCase.process(request)).thenThrow(new RuntimeException("failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.listServices(request).getStatusCode());
    }
}
