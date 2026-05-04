package com.os.workshop.features.service;

import com.os.workshop.features.service.create.CreateServiceHandler;
import com.os.workshop.features.service.create.CreateServiceRequest;
import com.os.workshop.features.service.findById.FindServiceByIdHandler;
import com.os.workshop.features.service.findByServiceOrder.FindServicesByServiceOrderHandler;
import com.os.workshop.features.service.list.ListServicesHandler;
import com.os.workshop.features.service.listTypes.ListServiceTypesHandler;
import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceTypeEntity;
import com.os.workshop.features.service.update.UpdateServiceHandler;
import com.os.workshop.features.service.update.UpdateServiceRequest;
import com.os.workshop.features.service.updateStatus.UpdateServiceStatusHandler;
import com.os.workshop.features.service.updateStatus.UpdateServiceStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceControllerTest {

    private final CreateServiceHandler createServiceHandler = mock(CreateServiceHandler.class);
    private final FindServiceByIdHandler findServiceByIdHandler = mock(FindServiceByIdHandler.class);
    private final FindServicesByServiceOrderHandler findServicesByServiceOrderHandler = mock(FindServicesByServiceOrderHandler.class);
    private final ListServicesHandler listServicesHandler = mock(ListServicesHandler.class);
    private final ListServiceTypesHandler listServiceTypesHandler = mock(ListServiceTypesHandler.class);
    private final UpdateServiceHandler updateServiceHandler = mock(UpdateServiceHandler.class);
    private final UpdateServiceStatusHandler updateServiceStatusHandler = mock(UpdateServiceStatusHandler.class);

    private final ServiceController controller = new ServiceController(
            createServiceHandler, findServiceByIdHandler, findServicesByServiceOrderHandler,
            listServicesHandler, listServiceTypesHandler, updateServiceHandler, updateServiceStatusHandler);

    @Test
    void returnsCreatedWhenServiceIsCreated() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setServiceType("REVISAO");
        request.setIdOS(UUID.randomUUID());
        when(createServiceHandler.handle(request)).thenReturn(new ServiceEntity());
        assertEquals(HttpStatus.CREATED, controller.createService(request).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenCreateFails() {
        CreateServiceRequest request = new CreateServiceRequest();
        when(createServiceHandler.handle(request)).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createService(request).getStatusCode());
    }

    @Test
    void returnsServiceWhenFound() {
        UUID id = UUID.randomUUID();
        when(findServiceByIdHandler.handle(id)).thenReturn(new ServiceEntity());
        assertEquals(HttpStatus.OK, controller.findById(id).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenServiceNotFound() {
        UUID id = UUID.randomUUID();
        when(findServiceByIdHandler.handle(id)).thenThrow(new RuntimeException("not found"));
        assertEquals(HttpStatus.NOT_FOUND, controller.findById(id).getStatusCode());
    }

    @Test
    void returnsServicesByOrderId() {
        UUID id = UUID.randomUUID();
        when(findServicesByServiceOrderHandler.handle(id)).thenReturn(List.of(new ServiceEntity()));
        assertEquals(HttpStatus.OK, controller.findByIdOS(id).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenFindByOSFails() {
        UUID id = UUID.randomUUID();
        when(findServicesByServiceOrderHandler.handle(id)).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.findByIdOS(id).getStatusCode());
    }

    @Test
    void returnsServices() {
        when(listServicesHandler.handle()).thenReturn(List.of(new ServiceEntity()));
        assertEquals(HttpStatus.OK, controller.listServices().getStatusCode());
    }

    @Test
    void returnsServerErrorWhenListFails() {
        when(listServicesHandler.handle()).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.listServices().getStatusCode());
    }

    @Test
    void returnsServiceTypes() {
        when(listServiceTypesHandler.handle()).thenReturn(List.of(new ServiceTypeEntity(UUID.randomUUID(), "REVISAO", "Revisao")));
        assertEquals(HttpStatus.OK, controller.listServiceTypes().getStatusCode());
    }

    @Test
    void returnsServerErrorWhenListTypesFails() {
        when(listServiceTypesHandler.handle()).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.listServiceTypes().getStatusCode());
    }

    @Test
    void returnsUpdatedService() {
        UUID id = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();
        when(updateServiceHandler.handle(id, request)).thenReturn(new ServiceEntity());
        assertEquals(HttpStatus.OK, controller.update(id, request).getStatusCode());
    }

    @Test
    void returnsNotFoundWhenUpdateFails() {
        UUID id = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();
        when(updateServiceHandler.handle(id, request)).thenThrow(new RuntimeException("not found"));
        assertEquals(HttpStatus.NOT_FOUND, controller.update(id, request).getStatusCode());
    }

    @Test
    void returnsUpdatedStatus() {
        UpdateServiceStatusRequest request = new UpdateServiceStatusRequest();
        when(updateServiceStatusHandler.handle(request)).thenReturn(new ServiceEntity());
        assertEquals(HttpStatus.OK, controller.updateStatus(request).getStatusCode());
    }

    @Test
    void returnsServerErrorWhenUpdateStatusFails() {
        UpdateServiceStatusRequest request = new UpdateServiceStatusRequest();
        when(updateServiceStatusHandler.handle(request)).thenThrow(new RuntimeException("failure"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.updateStatus(request).getStatusCode());
    }
}
