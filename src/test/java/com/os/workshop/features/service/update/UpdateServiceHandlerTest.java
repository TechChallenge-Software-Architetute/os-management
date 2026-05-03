package com.os.workshop.features.service.update;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceTypeEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import com.os.workshop.features.service.shared.repository.ServiceTypeRepository;
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
    private UpdateServiceHandler handler;

    @Test
    void updatesServiceTypeAndOrderId() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        ServiceEntity service = new ServiceEntity();
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setServiceType("TROCA");
        request.setIdOS(orderId);

        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
        when(serviceTypeRepository.findByName("TROCA"))
                .thenReturn(Optional.of(new ServiceTypeEntity(UUID.randomUUID(), "TROCA", "Troca")));
        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity result = handler.handle(id, request);

        assertEquals("TROCA", result.getServiceTypeName());
        assertEquals(orderId, result.getIdOS());
    }

    @Test
    void throwsWhenServiceDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> handler.handle(id, request));
    }

    @Test
    void throwsWhenServiceTypeDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setServiceType("TROCA");
        when(serviceRepository.findById(id)).thenReturn(Optional.of(new ServiceEntity()));
        when(serviceTypeRepository.findByName("TROCA")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> handler.handle(id, request));
    }
}
