package com.os.workshop.features.service.updateStatus;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.domain.ServiceStatusEnum;
import com.os.workshop.features.service.shared.domain.Status;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
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
    private UpdateServiceStatusHandler handler;

    private ServiceEntity createServiceEntity(UUID id) {
        ServiceEntity entity = new ServiceEntity();
        entity.setId(id);
        entity.setServiceTypeName("TROCA_OLEO");
        entity.setIdOS(UUID.randomUUID());
        ArrayList<Status> statusList = new ArrayList<>();
        statusList.add(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now()));
        entity.setServiceStatus(statusList);
        return entity;
    }

    private UpdateServiceStatusRequest createUpdateRequest(UUID id, ServiceStatusEnum status) {
        UpdateServiceStatusRequest request = new UpdateServiceStatusRequest();
        request.setId(id);
        request.setStatus(status);
        return request;
    }

    @Test
    void whenUpdatingStatusOfExistingService_thenNewStatusIsAdded() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        UpdateServiceStatusRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DOING);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = handler.handle(request);

        assertNotNull(result);
        assertEquals(2, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertEquals(ServiceStatusEnum.DOING, result.getServiceStatus().get(1).getStatus());
        verify(serviceRepository).save(service);
    }

    @Test
    void whenUpdatingStatusOfNonExistingService_thenThrowsRuntimeException() {
        UUID serviceId = UUID.randomUUID();
        UpdateServiceStatusRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DOING);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> handler.handle(request));

        assertEquals("Serviço não encontrado. ID: " + serviceId, exception.getMessage());
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void whenUpdatingStatusToDone_thenStatusListContainsAllTransitions() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        service.getServiceStatus().add(new Status(ServiceStatusEnum.DOING, LocalDateTime.now()));

        UpdateServiceStatusRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DONE);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = handler.handle(request);

        assertEquals(3, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertEquals(ServiceStatusEnum.DOING, result.getServiceStatus().get(1).getStatus());
        assertEquals(ServiceStatusEnum.DONE, result.getServiceStatus().get(2).getStatus());
    }

    @Test
    void whenUpdatingStatus_thenNewStatusHasTimestamp() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        UpdateServiceStatusRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DOING);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = handler.handle(request);

        Status newStatus = result.getServiceStatus().get(1);
        assertNotNull(newStatus.getChangedAt());
        assertEquals(ServiceStatusEnum.DOING, newStatus.getStatus());
    }

    @Test
    void whenUpdatingStatus_thenServiceIsSavedToRepository() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        UpdateServiceStatusRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DONE);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        handler.handle(request);

        verify(serviceRepository).findById(serviceId);
        verify(serviceRepository).save(service);
    }
}
