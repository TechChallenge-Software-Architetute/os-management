package com.os.workshop.features.service;

import com.os.workshop.adapter.in.web.service.*;
import com.os.workshop.application.service.*;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.ServiceType;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceControllerTest {

    private final CreateServiceUseCase createServiceUseCase = mock(CreateServiceUseCase.class);
    private final FindServiceByIdUseCase findServiceByIdUseCase = mock(FindServiceByIdUseCase.class);
    private final FindServicesByServiceOrderUseCase findServicesByServiceOrderUseCase = mock(FindServicesByServiceOrderUseCase.class);
    private final ListServicesUseCase listServicesUseCase = mock(ListServicesUseCase.class);
    private final ListServiceTypesUseCase listServiceTypesUseCase = mock(ListServiceTypesUseCase.class);
    private final UpdateServiceUseCase updateServiceUseCase = mock(UpdateServiceUseCase.class);
    private final UpdateServiceStatusUseCase updateServiceStatusUseCase = mock(UpdateServiceStatusUseCase.class);

    private final ServiceController controller = new ServiceController(
            createServiceUseCase, findServiceByIdUseCase, findServicesByServiceOrderUseCase,
            listServicesUseCase, listServiceTypesUseCase, updateServiceUseCase, updateServiceStatusUseCase);

    private WorkshopService sampleService() {
        return new WorkshopService(UUID.randomUUID(), "REVISAO", UUID.randomUUID(),
                List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));
    }

    @Test
    void returnsCreatedWhenServiceIsCreated() {
        when(createServiceUseCase.execute("REVISAO", null)).thenReturn(sampleService());
        CreateServiceRequest request = new CreateServiceRequest();
        request.setServiceType("REVISAO");
        assertEquals(HttpStatus.CREATED, controller.createService(request).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenCreateFails() {
        when(createServiceUseCase.execute(null, null)).thenThrow(new RuntimeException("failure"));
        CreateServiceRequest request = new CreateServiceRequest();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createService(request).getStatusCode());
    }

    @Test
    void returnsServiceWhenFound() {
        UUID id = UUID.randomUUID();
        when(findServiceByIdUseCase.execute(id)).thenReturn(sampleService());
        assertEquals(HttpStatus.OK, controller.findById(id).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenServiceNotFound() {
        UUID id = UUID.randomUUID();
        when(findServiceByIdUseCase.execute(id)).thenThrow(new RuntimeException("not found"));
        assertEquals(HttpStatus.NOT_FOUND, controller.findById(id).getStatusCode());
    }

    @Test
    void returnsServicesByOrderId() {
        UUID id = UUID.randomUUID();
        when(findServicesByServiceOrderUseCase.execute(id)).thenReturn(List.of(sampleService()));
        assertEquals(HttpStatus.OK, controller.findByIdOS(id).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenFindByOSFails() {
        UUID id = UUID.randomUUID();
        when(findServicesByServiceOrderUseCase.execute(id)).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.findByIdOS(id).getStatusCode());
    }

    @Test
    void returnsServices() {
        when(listServicesUseCase.execute()).thenReturn(List.of(sampleService()));
        assertEquals(HttpStatus.OK, controller.listServices().getStatusCode());
    }

    @Test
    void returnsServerErrorWhenListFails() {
        when(listServicesUseCase.execute()).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.listServices().getStatusCode());
    }

    @Test
    void returnsServiceTypes() {
        when(listServiceTypesUseCase.execute()).thenReturn(List.of(new ServiceType(UUID.randomUUID(), "REVISAO", "Revisao")));
        assertEquals(HttpStatus.OK, controller.listServiceTypes().getStatusCode());
    }

    @Test
    void returnsServerErrorWhenListTypesFails() {
        when(listServiceTypesUseCase.execute()).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.listServiceTypes().getStatusCode());
    }

    @Test
    void returnsUpdatedService() {
        UUID id = UUID.randomUUID();
        when(updateServiceUseCase.execute(id, "REVISAO", null)).thenReturn(sampleService());
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setServiceType("REVISAO");
        assertEquals(HttpStatus.OK, controller.update(id, request).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenUpdateFails() {
        UUID id = UUID.randomUUID();
        when(updateServiceUseCase.execute(id, null, null)).thenThrow(new RuntimeException("not found"));
        UpdateServiceRequest request = new UpdateServiceRequest();
        assertEquals(HttpStatus.NOT_FOUND, controller.update(id, request).getStatusCode());
    }

    @Test
    void returnsUpdatedStatus() {
        UUID id = UUID.randomUUID();
        when(updateServiceStatusUseCase.execute(id, ServiceStatusEnum.DOING)).thenReturn(sampleService());
        UpdateServiceStatusRequest request = new UpdateServiceStatusRequest();
        request.setId(id);
        request.setStatus(ServiceStatusEnum.DOING);
        assertEquals(HttpStatus.OK, controller.updateStatus(request).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUpdateStatusFails() {
        UUID id = UUID.randomUUID();
        when(updateServiceStatusUseCase.execute(id, ServiceStatusEnum.DOING)).thenThrow(new RuntimeException("failure"));
        UpdateServiceStatusRequest request = new UpdateServiceStatusRequest();
        request.setId(id);
        request.setStatus(ServiceStatusEnum.DOING);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.updateStatus(request).getStatusCode());
    }
}
