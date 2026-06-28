package com.os.workshop.features.service.findByServiceOrder;

import com.os.workshop.application.service.FindServicesByServiceOrderUseCase;
import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.service.WorkshopService;
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
    private FindServicesByServiceOrderUseCase useCase;

    @Test
    void returnsServicesForOrderId() {
        UUID orderId = UUID.randomUUID();
        when(serviceRepository.findByIdOS(orderId)).thenReturn(List.of(new WorkshopService()));

        assertEquals(1, useCase.execute(orderId).size());
    }
}
