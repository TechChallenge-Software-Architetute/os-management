package com.os.workshop.features.service.findById;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindServiceByIdHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private FindServiceByIdHandler findServiceByIdHandler;

    private ServiceEntity createServiceEntity(UUID id) {
        ServiceEntity entity = new ServiceEntity();
        entity.setId(id);
        entity.setServiceTypeName("TROCA_OLEO");
        entity.setIdOS(UUID.randomUUID());
        entity.setServiceStatus(List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));
        return entity;
    }

    @Test
    void whenFindingServiceByExistingId_thenReturnsService() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity expected = createServiceEntity(serviceId);
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(expected));

        ServiceEntity result = findServiceByIdHandler.handle(serviceId);

        assertNotNull(result);
        assertEquals(serviceId, result.getId());
        assertEquals("TROCA_OLEO", result.getServiceTypeName());
        verify(serviceRepository).findById(serviceId);
    }

    @Test
    void whenFindingServiceByNonExistingId_thenThrowsRuntimeException() {
        UUID serviceId = UUID.randomUUID();
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> findServiceByIdHandler.handle(serviceId));

        assertEquals("Servico nao encontrado. ID: " + serviceId, exception.getMessage());
        verify(serviceRepository).findById(serviceId);
    }

    @Test
    void whenFindingServiceByExistingId_thenReturnsCorrectServiceStatus() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity expected = createServiceEntity(serviceId);
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(expected));

        ServiceEntity result = findServiceByIdHandler.handle(serviceId);

        assertNotNull(result.getServiceStatus());
        assertEquals(1, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
    }

    @Test
    void whenFindingServiceByExistingId_thenReturnsCorrectIdOS() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity expected = createServiceEntity(serviceId);
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(expected));

        ServiceEntity result = findServiceByIdHandler.handle(serviceId);

        assertEquals(expected.getIdOS(), result.getIdOS());
    }
}
