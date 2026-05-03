package com.os.workshop.features.service.findByServiceOrder;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindServicesByServiceOrderHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private FindServicesByServiceOrderHandler handler;

    @Test
    void returnsServicesForOrderId() {
        UUID orderId = UUID.randomUUID();
        ServiceEntity service = new ServiceEntity();
        when(serviceRepository.findByIdOS(orderId)).thenReturn(List.of(service));

        assertEquals(1, handler.handle(orderId).size());
    }
}
