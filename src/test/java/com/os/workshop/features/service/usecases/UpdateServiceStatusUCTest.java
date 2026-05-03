package com.os.workshop.features.service.usecases;

import com.os.workshop.features.service.adapter.database.ServiceRepository;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.Status;
import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import com.os.workshop.features.service.domain.requests.UpdateStatusServiceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateServiceStatusUCTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private UpdateServiceStatusUC updateServiceStatusUC;

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

    private UpdateStatusServiceRequest createUpdateRequest(UUID id, ServiceStatusEnum status) {
        UpdateStatusServiceRequest request = new UpdateStatusServiceRequest();
        request.setId(id);
        request.setStatus(status);
        return request;
    }

    @Test
    void whenUpdatingStatusOfExistingService_thenNewStatusIsAdded() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        UpdateStatusServiceRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DOING);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = updateServiceStatusUC.process(request);

        assertNotNull(result);
        assertEquals(2, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertEquals(ServiceStatusEnum.DOING, result.getServiceStatus().get(1).getStatus());
        verify(serviceRepository).save(service);
    }

    @Test
    void whenUpdatingStatusOfNonExistingService_thenThrowsRuntimeException() {
        UUID serviceId = UUID.randomUUID();
        UpdateStatusServiceRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DOING);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> updateServiceStatusUC.process(request));

        assertEquals("Serviço não encontrado. ID: " + serviceId, exception.getMessage());
        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void whenUpdatingStatusToDone_thenStatusListContainsAllTransitions() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        // First transition: TO_DO -> DOING
        service.getServiceStatus().add(new Status(ServiceStatusEnum.DOING, LocalDateTime.now()));

        UpdateStatusServiceRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DONE);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = updateServiceStatusUC.process(request);

        assertEquals(3, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
        assertEquals(ServiceStatusEnum.DOING, result.getServiceStatus().get(1).getStatus());
        assertEquals(ServiceStatusEnum.DONE, result.getServiceStatus().get(2).getStatus());
    }

    @Test
    void whenUpdatingStatus_thenNewStatusHasTimestamp() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        UpdateStatusServiceRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DOING);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        ServiceEntity result = updateServiceStatusUC.process(request);

        Status newStatus = result.getServiceStatus().get(1);
        assertNotNull(newStatus.getChangedAt());
        assertEquals(ServiceStatusEnum.DOING, newStatus.getStatus());
    }

    @Test
    void whenUpdatingStatus_thenServiceIsSavedToRepository() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = createServiceEntity(serviceId);
        UpdateStatusServiceRequest request = createUpdateRequest(serviceId, ServiceStatusEnum.DONE);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        updateServiceStatusUC.process(request);

        verify(serviceRepository).findById(serviceId);
        verify(serviceRepository).save(service);
    }
}
