package com.os.workshop.features.service.updateStatus;

import com.os.workshop.application.service.UpdateServiceStatusUseCase;
import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateServiceStatusHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private UpdateServiceStatusUseCase useCase;

    private WorkshopService createService(UUID id) {
        ArrayList<Status> statusList = new ArrayList<>();
        statusList.add(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now()));
        return new WorkshopService(id, "TROCA_OLEO", UUID.randomUUID(), statusList);
    }

    @Test
    void whenUpdatingStatusOfExistingService_thenNewStatusIsAdded() {
        UUID serviceId = UUID.randomUUID();
        WorkshopService service = createService(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

        WorkshopService result = useCase.execute(serviceId, ServiceStatusEnum.DOING);

        assertNotNull(result);
        assertEquals(2, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertEquals(ServiceStatusEnum.DOING, result.getServiceStatus().get(1).getStatus());
        verify(serviceRepository).save(service);
    }

    @Test
    void whenUpdatingStatusOfNonExistingService_thenThrowsRuntimeException() {
        UUID serviceId = UUID.randomUUID();
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> useCase.execute(serviceId, ServiceStatusEnum.DOING));

        assertEquals("Serviço não encontrado. ID: " + serviceId, exception.getMessage());
        verify(serviceRepository, never()).save(any(WorkshopService.class));
    }

    @Test
    void whenUpdatingStatusToDone_thenStatusListContainsAllTransitions() {
        UUID serviceId = UUID.randomUUID();
        WorkshopService service = createService(serviceId);
        service.getServiceStatus().add(new Status(ServiceStatusEnum.DOING, LocalDateTime.now()));

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

        WorkshopService result = useCase.execute(serviceId, ServiceStatusEnum.DONE);

        assertEquals(3, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertEquals(ServiceStatusEnum.DOING, result.getServiceStatus().get(1).getStatus());
        assertEquals(ServiceStatusEnum.DONE, result.getServiceStatus().get(2).getStatus());
    }

    @Test
    void whenUpdatingStatus_thenNewStatusHasTimestamp() {
        UUID serviceId = UUID.randomUUID();
        WorkshopService service = createService(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

        WorkshopService result = useCase.execute(serviceId, ServiceStatusEnum.DOING);

        Status newStatus = result.getServiceStatus().get(1);
        assertNotNull(newStatus.getChangedAt());
        assertEquals(ServiceStatusEnum.DOING, newStatus.getStatus());
    }

    @Test
    void whenUpdatingStatus_thenServiceIsSavedToRepository() {
        UUID serviceId = UUID.randomUUID();
        WorkshopService service = createService(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(WorkshopService.class))).thenAnswer(i -> i.getArgument(0));

        useCase.execute(serviceId, ServiceStatusEnum.DONE);

        verify(serviceRepository).findById(serviceId);
        verify(serviceRepository).save(service);
    }
}
