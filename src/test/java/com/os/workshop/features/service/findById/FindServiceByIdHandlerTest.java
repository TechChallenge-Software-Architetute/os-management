package com.os.workshop.features.service.findById;

import com.os.workshop.application.service.FindServiceByIdUseCase;
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
    private FindServiceByIdUseCase findServiceByIdUseCase;

    private WorkshopService createService(UUID id) {
        return new WorkshopService(id, "TROCA_OLEO", UUID.randomUUID(),
                List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));
    }

    @Test
    void whenFindingServiceByExistingId_thenReturnsService() {
        UUID serviceId = UUID.randomUUID();
        WorkshopService expected = createService(serviceId);
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(expected));

        WorkshopService result = findServiceByIdUseCase.execute(serviceId);

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
                () -> findServiceByIdUseCase.execute(serviceId));

        assertEquals("Servico nao encontrado. ID: " + serviceId, exception.getMessage());
        verify(serviceRepository).findById(serviceId);
    }

    @Test
    void whenFindingServiceByExistingId_thenReturnsCorrectServiceStatus() {
        UUID serviceId = UUID.randomUUID();
        WorkshopService expected = createService(serviceId);
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(expected));

        WorkshopService result = findServiceByIdUseCase.execute(serviceId);

        assertNotNull(result.getServiceStatus());
        assertEquals(1, result.getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, result.getServiceStatus().get(0).getStatus());
    }

    @Test
    void whenFindingServiceByExistingId_thenReturnsCorrectIdOS() {
        UUID serviceId = UUID.randomUUID();
        WorkshopService expected = createService(serviceId);
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(expected));

        WorkshopService result = findServiceByIdUseCase.execute(serviceId);

        assertEquals(expected.getIdOS(), result.getIdOS());
    }
}
