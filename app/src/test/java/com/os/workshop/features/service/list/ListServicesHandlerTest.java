package com.os.workshop.features.service.list;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListServicesHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ListServicesHandler handler;

    @Test
    void returnsAllServices() {
        when(serviceRepository.findAll()).thenReturn(List.of(new ServiceEntity()));
        assertEquals(1, handler.handle().size());
    }

    @Test
    void propagatesRepositoryException() {
        when(serviceRepository.findAll()).thenThrow(new RuntimeException("failure"));
        assertThrows(RuntimeException.class, () -> handler.handle());
    }
}
