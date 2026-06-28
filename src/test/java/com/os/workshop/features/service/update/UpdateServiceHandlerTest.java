package com.os.workshop.features.service.update;

import com.os.workshop.application.service.UpdateServiceUseCase;
import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.application.service.port.out.ServiceTypeRepository;
import com.os.workshop.domain.service.ServiceType;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateServiceHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @InjectMocks
    private UpdateServiceUseCase useCase;

    @Test
    void updatesServiceTypeAndOrderId() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        WorkshopService service = new WorkshopService();

        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
        when(serviceTypeRepository.findByName("TROCA"))
                .thenReturn(Optional.of(new ServiceType(UUID.randomUUID(), "TROCA", "Troca")));
        when(serviceRepository.save(service)).thenReturn(service);

        WorkshopService result = useCase.execute(id, "TROCA", orderId);

        assertEquals("TROCA", result.getServiceTypeName());
        assertEquals(orderId, result.getIdOS());
    }

    @Test
    void throwsWhenServiceDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> useCase.execute(id, "TROCA", UUID.randomUUID()));
    }

    @Test
    void throwsWhenServiceTypeDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(serviceRepository.findById(id)).thenReturn(Optional.of(new WorkshopService()));
        when(serviceTypeRepository.findByName("TROCA")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> useCase.execute(id, "TROCA", UUID.randomUUID()));
    }
}
