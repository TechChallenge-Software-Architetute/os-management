package com.os.workshop.application.service;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.application.service.port.out.ServiceTypeRepository;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.ServiceType;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceUseCaseTest {

    @Mock private ServiceRepository serviceRepository;
    @Mock private ServiceTypeRepository serviceTypeRepository;

    // ========================= CreateServiceUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class CreateServiceUseCaseTests {

        @Mock private ServiceRepository serviceRepository;
        @Mock private ServiceTypeRepository serviceTypeRepository;
        @InjectMocks private CreateServiceUseCase useCase;

        @Test
        void createsServiceSuccessfully() {
            UUID idOS = UUID.randomUUID();
            ServiceType type = new ServiceType(UUID.randomUUID(), "Troca de Óleo", "Troca completa");
            when(serviceTypeRepository.findByName("Troca de Óleo")).thenReturn(Optional.of(type));
            when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> {
                WorkshopService s = i.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });

            WorkshopService result = useCase.execute("Troca de Óleo", idOS);

            assertNotNull(result);
            assertEquals("Troca de Óleo", result.getServiceTypeName());
            assertEquals(idOS, result.getIdOS());
            assertEquals(1, result.getServiceStatus().size());
            assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
            verify(serviceRepository).save(any(WorkshopService.class));
        }

        @Test
        void throwsWhenServiceTypeNotFound() {
            UUID idOS = UUID.randomUUID();
            when(serviceTypeRepository.findByName("Inexistente")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> useCase.execute("Inexistente", idOS));

            assertTrue(ex.getMessage().contains("Tipo de serviço não encontrado"));
            verify(serviceRepository, never()).save(any());
        }

        @Test
        void setsInitialStatusAsToDo() {
            UUID idOS = UUID.randomUUID();
            ServiceType type = new ServiceType(UUID.randomUUID(), "Alinhamento", "Alinhamento de rodas");
            when(serviceTypeRepository.findByName("Alinhamento")).thenReturn(Optional.of(type));
            when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

            WorkshopService result = useCase.execute("Alinhamento", idOS);

            assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
            assertNotNull(result.getServiceStatus().get(0).getChangedAt());
        }
    }

    // ========================= FindServiceByIdUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class FindServiceByIdUseCaseTests {

        @Mock private ServiceRepository serviceRepository;
        @InjectMocks private FindServiceByIdUseCase useCase;

        @Test
        void returnsServiceWhenFound() {
            UUID id = UUID.randomUUID();
            WorkshopService service = new WorkshopService(id, "Pintura", UUID.randomUUID(), List.of());
            when(serviceRepository.findById(id)).thenReturn(Optional.of(service));

            WorkshopService result = useCase.execute(id);

            assertEquals(id, result.getId());
            assertEquals("Pintura", result.getServiceTypeName());
        }

        @Test
        void throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(serviceRepository.findById(id)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.execute(id));

            assertTrue(ex.getMessage().contains("Servico nao encontrado"));
            assertTrue(ex.getMessage().contains(id.toString()));
        }
    }

    // ========================= ListServicesUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ListServicesUseCaseTests {

        @Mock private ServiceRepository serviceRepository;
        @InjectMocks private ListServicesUseCase useCase;

        @Test
        void returnsAllServices() {
            List<WorkshopService> services = List.of(
                    new WorkshopService(UUID.randomUUID(), "Pintura", UUID.randomUUID(), List.of()),
                    new WorkshopService(UUID.randomUUID(), "Troca de Óleo", UUID.randomUUID(), List.of())
            );
            when(serviceRepository.findAll()).thenReturn(services);

            List<WorkshopService> result = useCase.execute();

            assertEquals(2, result.size());
            verify(serviceRepository).findAll();
        }

        @Test
        void returnsEmptyListWhenNoServices() {
            when(serviceRepository.findAll()).thenReturn(List.of());

            List<WorkshopService> result = useCase.execute();

            assertTrue(result.isEmpty());
        }
    }

    // ========================= ListServiceTypesUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ListServiceTypesUseCaseTests {

        @Mock private ServiceTypeRepository serviceTypeRepository;
        @InjectMocks private ListServiceTypesUseCase useCase;

        @Test
        void returnsAllServiceTypes() {
            List<ServiceType> types = List.of(
                    new ServiceType(UUID.randomUUID(), "Pintura", "Pintura automotiva"),
                    new ServiceType(UUID.randomUUID(), "Mecânica", "Serviços mecânicos")
            );
            when(serviceTypeRepository.findAll()).thenReturn(types);

            List<ServiceType> result = useCase.execute();

            assertEquals(2, result.size());
            verify(serviceTypeRepository).findAll();
        }

        @Test
        void returnsEmptyListWhenNoTypes() {
            when(serviceTypeRepository.findAll()).thenReturn(List.of());

            List<ServiceType> result = useCase.execute();

            assertTrue(result.isEmpty());
        }
    }

    // ========================= UpdateServiceUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class UpdateServiceUseCaseTests {

        @Mock private ServiceRepository serviceRepository;
        @Mock private ServiceTypeRepository serviceTypeRepository;
        @InjectMocks private UpdateServiceUseCase useCase;

        @Test
        void updatesServiceSuccessfully() {
            UUID id = UUID.randomUUID();
            UUID newIdOS = UUID.randomUUID();
            WorkshopService existing = new WorkshopService(id, "Pintura", UUID.randomUUID(), List.of());
            ServiceType newType = new ServiceType(UUID.randomUUID(), "Mecânica", "Serviços mecânicos");

            when(serviceRepository.findById(id)).thenReturn(Optional.of(existing));
            when(serviceTypeRepository.findByName("Mecânica")).thenReturn(Optional.of(newType));
            when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

            WorkshopService result = useCase.execute(id, "Mecânica", newIdOS);

            assertEquals("Mecânica", result.getServiceTypeName());
            assertEquals(newIdOS, result.getIdOS());
            verify(serviceRepository).save(existing);
        }

        @Test
        void throwsWhenServiceNotFound() {
            UUID id = UUID.randomUUID();
            when(serviceRepository.findById(id)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> useCase.execute(id, "Pintura", UUID.randomUUID()));

            assertTrue(ex.getMessage().contains("Servico nao encontrado"));
        }

        @Test
        void throwsWhenServiceTypeNotFound() {
            UUID id = UUID.randomUUID();
            WorkshopService existing = new WorkshopService(id, "Pintura", UUID.randomUUID(), List.of());
            when(serviceRepository.findById(id)).thenReturn(Optional.of(existing));
            when(serviceTypeRepository.findByName("Invalido")).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> useCase.execute(id, "Invalido", UUID.randomUUID()));

            assertTrue(ex.getMessage().contains("Tipo de servico nao encontrado"));
        }
    }

    // ========================= UpdateServiceStatusUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class UpdateServiceStatusUseCaseTests {

        @Mock private ServiceRepository serviceRepository;
        @InjectMocks private UpdateServiceStatusUseCase useCase;

        @Test
        void updatesStatusSuccessfully() {
            UUID id = UUID.randomUUID();
            List<Status> statusList = new ArrayList<>();
            statusList.add(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now()));
            WorkshopService service = new WorkshopService(id, "Pintura", UUID.randomUUID(), statusList);

            when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
            when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

            WorkshopService result = useCase.execute(id, ServiceStatusEnum.DOING);

            assertEquals(2, result.getServiceStatus().size());
            assertEquals(ServiceStatusEnum.DOING, result.getServiceStatus().get(1).getStatus());
            verify(serviceRepository).save(service);
        }

        @Test
        void throwsWhenServiceNotFound() {
            UUID id = UUID.randomUUID();
            when(serviceRepository.findById(id)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> useCase.execute(id, ServiceStatusEnum.DONE));

            assertTrue(ex.getMessage().contains("Serviço não encontrado"));
        }

        @Test
        void addsNewStatusToExistingList() {
            UUID id = UUID.randomUUID();
            List<Status> statusList = new ArrayList<>();
            statusList.add(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now().minusDays(1)));
            statusList.add(new Status(ServiceStatusEnum.DOING, LocalDateTime.now().minusHours(2)));
            WorkshopService service = new WorkshopService(id, "Mecânica", UUID.randomUUID(), statusList);

            when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
            when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

            WorkshopService result = useCase.execute(id, ServiceStatusEnum.DONE);

            assertEquals(3, result.getServiceStatus().size());
            assertEquals(ServiceStatusEnum.DONE, result.getServiceStatus().get(2).getStatus());
        }
    }

    // ========================= FindServicesByServiceOrderUseCase =========================

    @Nested
    @ExtendWith(MockitoExtension.class)
    class FindServicesByServiceOrderUseCaseTests {

        @Mock private ServiceRepository serviceRepository;
        @InjectMocks private FindServicesByServiceOrderUseCase useCase;

        @Test
        void returnsServicesForOrder() {
            UUID idOS = UUID.randomUUID();
            List<WorkshopService> services = List.of(
                    new WorkshopService(UUID.randomUUID(), "Pintura", idOS, List.of()),
                    new WorkshopService(UUID.randomUUID(), "Mecânica", idOS, List.of())
            );
            when(serviceRepository.findByIdOS(idOS)).thenReturn(services);

            List<WorkshopService> result = useCase.execute(idOS);

            assertEquals(2, result.size());
            verify(serviceRepository).findByIdOS(idOS);
        }

        @Test
        void returnsEmptyListWhenNoServicesForOrder() {
            UUID idOS = UUID.randomUUID();
            when(serviceRepository.findByIdOS(idOS)).thenReturn(List.of());

            List<WorkshopService> result = useCase.execute(idOS);

            assertTrue(result.isEmpty());
        }
    }
}
