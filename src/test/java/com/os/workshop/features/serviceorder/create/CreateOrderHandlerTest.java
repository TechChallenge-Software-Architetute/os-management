package com.os.workshop.features.serviceorder.create;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.client.findByCpf.FindClientByCpfHandler;
import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.CreateServiceRequest;
import com.os.workshop.features.service.usecases.CreateServiceUC;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import com.os.workshop.features.vehicle.VehicleService;
import com.os.workshop.features.vehicle.domain.Vehicle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderHandlerTest {

    @Mock
    private CreateServiceUC createServiceUC;

    @Mock
    private FindClientByCpfHandler findClientByCpfHandler;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Mock
    private FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    @InjectMocks
    private CreateOrderHandler createOrderHandler;

    private CreateOrderRequest createRequest(String cpf, String placa, List<String> serviceTypes) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCpfCnpj(cpf);
        request.setPlacaVeiculo(placa);
        request.setServiceTypes(serviceTypes);
        return request;
    }

    @Test
    void whenCreatingOrderWithValidData_thenOrderIsSaved() {
        CreateOrderRequest request = createRequest("12345678900", "ABC1234", List.of("TROCA_OLEO", "ALINHAMENTO"));

        when(findClientByCpfHandler.handle("12345678900")).thenReturn(mock(Client.class));
        when(vehicleService.findByPlate("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUC.process(any(CreateServiceRequest.class))).thenReturn(new ServiceEntity());
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        ServiceOrder result = createOrderHandler.handle(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), result.getServiceStatus());
        assertEquals("12345678900", result.getCpfCnpj());
        assertEquals("ABC1234", result.getPlacaVeiculo());
        assertEquals(List.of("TROCA_OLEO", "ALINHAMENTO"), result.getListService());
        verify(serviceOrderJpaRepository).save(any(ServiceOrderEntity.class));
    }

    @Test
    void whenCreatingOrderWithMultipleServices_thenEachServiceIsCreated() {
        List<String> serviceTypes = List.of("TROCA_OLEO", "ALINHAMENTO", "BALANCEAMENTO");
        CreateOrderRequest request = createRequest("12345678900", "ABC1234", serviceTypes);

        when(findClientByCpfHandler.handle("12345678900")).thenReturn(mock(Client.class));
        when(vehicleService.findByPlate("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUC.process(any(CreateServiceRequest.class))).thenReturn(new ServiceEntity());
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        createOrderHandler.handle(request);

        verify(createServiceUC, times(3)).process(any(CreateServiceRequest.class));
    }

    @Test
    void whenCreatingOrderWithInvalidClient_thenThrowsException() {
        CreateOrderRequest request = createRequest("00000000000", "ABC1234", List.of("TROCA_OLEO"));

        when(findClientByCpfHandler.handle("00000000000")).thenThrow(new RuntimeException("Client not found"));

        assertThrows(RuntimeException.class, () -> createOrderHandler.handle(request));

        verify(serviceOrderJpaRepository, never()).save(any(ServiceOrderEntity.class));
    }

    @Test
    void whenCreatingOrderWithInvalidVehicle_thenThrowsException() {
        CreateOrderRequest request = createRequest("12345678900", "INVALID", List.of("TROCA_OLEO"));

        when(findClientByCpfHandler.handle("12345678900")).thenReturn(mock(Client.class));
        when(vehicleService.findByPlate("INVALID")).thenThrow(new RuntimeException("Vehicle not found"));

        assertThrows(RuntimeException.class, () -> createOrderHandler.handle(request));

        verify(serviceOrderJpaRepository, never()).save(any(ServiceOrderEntity.class));
    }

    @Test
    void whenCreatingOrder_thenInitialStatusIsRecebida() {
        CreateOrderRequest request = createRequest("12345678900", "ABC1234", List.of("TROCA_OLEO"));

        when(findClientByCpfHandler.handle("12345678900")).thenReturn(mock(Client.class));
        when(vehicleService.findByPlate("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUC.process(any(CreateServiceRequest.class))).thenReturn(new ServiceEntity());
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        ServiceOrder result = createOrderHandler.handle(request);

        assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), result.getServiceStatus());
    }

    @Test
    void whenCreatingOrder_thenClientAndVehicleAreValidated() {
        CreateOrderRequest request = createRequest("12345678900", "ABC1234", List.of("TROCA_OLEO"));

        when(findClientByCpfHandler.handle("12345678900")).thenReturn(mock(Client.class));
        when(vehicleService.findByPlate("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUC.process(any(CreateServiceRequest.class))).thenReturn(new ServiceEntity());
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        createOrderHandler.handle(request);

        verify(findClientByCpfHandler).handle("12345678900");
        verify(vehicleService).findByPlate("ABC1234");
    }
}
