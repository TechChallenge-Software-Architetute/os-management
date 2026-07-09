package com.os.workshop.application.serviceorder;

import com.os.workshop.application.client.FindClientByCpfUseCase;
import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.domain.service.WorkshopService;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderUseCaseTest {

    // ========================= CreateOrderUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class CreateOrderUseCaseTests {

        @Mock private CreateServiceUseCase createServiceUseCase;
        @Mock private FindClientByCpfUseCase findClientByCpfUseCase;
        @Mock private FindVehicleByPlateUseCase findVehicleByPlateUseCase;
        @Mock private ServiceOrderRepository serviceOrderRepository;
        @InjectMocks private CreateOrderUseCase useCase;

        @Test
        void createsOrderSuccessfully() {
            String cpf = "12345678900";
            String placa = "ABC1234";
            List<String> serviceTypes = List.of("Pintura", "Mecânica");

            when(findClientByCpfUseCase.execute(cpf)).thenReturn(null);
            when(findVehicleByPlateUseCase.execute(placa)).thenReturn(null);
            when(createServiceUseCase.execute(anyString(), any(UUID.class))).thenReturn(new WorkshopService());
            when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

            ServiceOrder result = useCase.execute(cpf, placa, serviceTypes);

            assertNotNull(result);
            assertNotNull(result.getId());
            assertEquals(cpf, result.getCpfCnpj());
            assertEquals(placa, result.getPlacaVeiculo());
            assertEquals(OrderServiceStatusEnum.RECEBIDA.getStatus(), result.getServiceStatus());
            assertEquals(serviceTypes, result.getListService());
            verify(createServiceUseCase, times(2)).execute(anyString(), any(UUID.class));
            verify(serviceOrderRepository).save(any(ServiceOrder.class));
        }

        @Test
        void throwsWhenClientNotFound() {
            String cpf = "00000000000";
            when(findClientByCpfUseCase.execute(cpf)).thenThrow(new ClientNotFoundException("CPF: " + cpf));

            assertThrows(ClientNotFoundException.class,
                    () -> useCase.execute(cpf, "ABC1234", List.of("Pintura")));

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void throwsWhenVehicleNotFound() {
            String cpf = "12345678900";
            String placa = "XXX0000";
            when(findClientByCpfUseCase.execute(cpf)).thenReturn(null);
            when(findVehicleByPlateUseCase.execute(placa)).thenThrow(new VehicleNotFoundException("plate: " + placa));

            assertThrows(VehicleNotFoundException.class,
                    () -> useCase.execute(cpf, placa, List.of("Pintura")));

            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void createsServiceForEachType() {
            String cpf = "12345678900";
            String placa = "ABC1234";
            List<String> serviceTypes = List.of("Pintura", "Mecânica", "Elétrica");

            when(findClientByCpfUseCase.execute(cpf)).thenReturn(null);
            when(findVehicleByPlateUseCase.execute(placa)).thenReturn(null);
            when(createServiceUseCase.execute(anyString(), any(UUID.class))).thenReturn(new WorkshopService());
            when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

            useCase.execute(cpf, placa, serviceTypes);

            verify(createServiceUseCase, times(3)).execute(anyString(), any(UUID.class));
            verify(createServiceUseCase).execute(eq("Pintura"), any(UUID.class));
            verify(createServiceUseCase).execute(eq("Mecânica"), any(UUID.class));
            verify(createServiceUseCase).execute(eq("Elétrica"), any(UUID.class));
        }
    }

    // ========================= FindOrderByIdUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class FindOrderByIdUseCaseTests {

        @Mock private ServiceOrderRepository serviceOrderRepository;
        @InjectMocks private FindOrderByIdUseCase useCase;

        @Test
        void returnsOrderWhenFound() {
            UUID id = UUID.randomUUID();
            ServiceOrder order = ServiceOrder.builder()
                    .id(id)
                    .cpfCnpj("12345678900")
                    .placaVeiculo("ABC1234")
                    .serviceStatus(OrderServiceStatusEnum.RECEBIDA.getStatus())
                    .build();
            when(serviceOrderRepository.findById(id)).thenReturn(Optional.of(order));

            ServiceOrder result = useCase.execute(id);

            assertEquals(id, result.getId());
            assertEquals("12345678900", result.getCpfCnpj());
        }

        @Test
        void throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(serviceOrderRepository.findById(id)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.execute(id));

            assertTrue(ex.getMessage().contains("Ordem de servico nao encontrada"));
        }
    }

    // ========================= ListOrdersUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ListOrdersUseCaseTests {

        @Mock private ServiceOrderRepository serviceOrderRepository;
        @InjectMocks private ListOrdersUseCase useCase;

        @Test
        void returnsAllOrders() {
            List<ServiceOrder> orders = List.of(
                    ServiceOrder.builder().id(UUID.randomUUID()).cpfCnpj("111").build(),
                    ServiceOrder.builder().id(UUID.randomUUID()).cpfCnpj("222").build()
            );
            when(serviceOrderRepository.findAll()).thenReturn(orders);

            List<ServiceOrder> result = useCase.execute();

            assertEquals(2, result.size());
            verify(serviceOrderRepository).findAll();
        }

        @Test
        void returnsEmptyListWhenNoOrders() {
            when(serviceOrderRepository.findAll()).thenReturn(List.of());

            List<ServiceOrder> result = useCase.execute();

            assertTrue(result.isEmpty());
        }
    }

    // ========================= UpdateOrderUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class UpdateOrderUseCaseTests {

        @Mock private ServiceOrderRepository serviceOrderRepository;
        @InjectMocks private UpdateOrderUseCase useCase;

        @Test
        void updatesOrderStatusSuccessfully() {
            UUID id = UUID.randomUUID();
            ServiceOrder order = ServiceOrder.builder()
                    .id(id)
                    .serviceStatus(OrderServiceStatusEnum.RECEBIDA.getStatus())
                    .cpfCnpj("12345678900")
                    .build();

            when(serviceOrderRepository.findById(id)).thenReturn(Optional.of(order));
            when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(i -> i.getArgument(0));

            ServiceOrder result = useCase.execute(id, OrderServiceStatusEnum.EM_EXECUCAO);

            assertEquals(OrderServiceStatusEnum.EM_EXECUCAO.getStatus(), result.getServiceStatus());
            verify(serviceOrderRepository).save(order);
        }

        @Test
        void throwsWhenOrderNotFound() {
            UUID id = UUID.randomUUID();
            when(serviceOrderRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class,
                    () -> useCase.execute(id, OrderServiceStatusEnum.FINALIZADA));
        }

        @Test
        void throwsWhenStatusIsNull() {
            UUID id = UUID.randomUUID();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> useCase.execute(id, null));

            assertTrue(ex.getMessage().contains("Status da ordem de servico e obrigatorio"));
            verify(serviceOrderRepository, never()).findById(any());
        }
    }
}
