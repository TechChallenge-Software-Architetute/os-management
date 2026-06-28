package com.os.workshop.features.serviceorder.create;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.application.client.FindClientByCpfUseCase;
import com.os.workshop.domain.client.Client;
import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.domain.service.WorkshopService;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import com.os.workshop.domain.vehicle.Vehicle;
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
    private CreateServiceUseCase createServiceUseCase;

    @Mock
    private FindClientByCpfUseCase findClientByCpfUseCase;

    @Mock
    private FindVehicleByPlateUseCase findVehicleByPlateUseCase;

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

        when(findClientByCpfUseCase.execute("12345678900")).thenReturn(mock(Client.class));
        when(findVehicleByPlateUseCase.execute("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUseCase.execute(any(), any())).thenReturn(new WorkshopService());
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

        when(findClientByCpfUseCase.execute("12345678900")).thenReturn(mock(Client.class));
        when(findVehicleByPlateUseCase.execute("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUseCase.execute(any(), any())).thenReturn(new WorkshopService());
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        createOrderHandler.handle(request);

        verify(createServiceUseCase, times(3)).execute(any(), any());
    }

    @Test
    void whenCreatingOrderWithInvalidClient_thenThrowsException() {
        CreateOrderRequest request = createRequest("00000000000", "ABC1234", List.of("TROCA_OLEO"));

        when(findClientByCpfUseCase.execute("00000000000")).thenThrow(new RuntimeException("Client not found"));

        assertThrows(RuntimeException.class, () -> createOrderHandler.handle(request));

        verify(serviceOrderJpaRepository, never()).save(any(ServiceOrderEntity.class));
    }

    @Test
    void whenCreatingOrderWithInvalidVehicle_thenThrowsException() {
        CreateOrderRequest request = createRequest("12345678900", "INVALID", List.of("TROCA_OLEO"));

        when(findClientByCpfUseCase.execute("12345678900")).thenReturn(mock(Client.class));
        when(findVehicleByPlateUseCase.execute("INVALID")).thenThrow(new RuntimeException("Vehicle not found"));

        assertThrows(RuntimeException.class, () -> createOrderHandler.handle(request));

        verify(serviceOrderJpaRepository, never()).save(any(ServiceOrderEntity.class));
    }

    @Test
    void whenCreatingOrder_thenInitialStatusIsRecebida() {
        CreateOrderRequest request = createRequest("12345678900", "ABC1234", List.of("TROCA_OLEO"));

        when(findClientByCpfUseCase.execute("12345678900")).thenReturn(mock(Client.class));
        when(findVehicleByPlateUseCase.execute("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUseCase.execute(any(), any())).thenReturn(new WorkshopService());
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        ServiceOrder result = createOrderHandler.handle(request);

        assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), result.getServiceStatus());
    }

    @Test
    void whenCreatingOrder_thenClientAndVehicleAreValidated() {
        CreateOrderRequest request = createRequest("12345678900", "ABC1234", List.of("TROCA_OLEO"));

        when(findClientByCpfUseCase.execute("12345678900")).thenReturn(mock(Client.class));
        when(findVehicleByPlateUseCase.execute("ABC1234")).thenReturn(mock(Vehicle.class));
        when(createServiceUseCase.execute(any(), any())).thenReturn(new WorkshopService());
        when(serviceOrderJpaRepository.save(any(ServiceOrderEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(findBudgetByServiceOrderHandler.handle(any())).thenReturn(Optional.empty());

        createOrderHandler.handle(request);

        verify(findClientByCpfUseCase).execute("12345678900");
        verify(findVehicleByPlateUseCase).execute("ABC1234");
    }
}
