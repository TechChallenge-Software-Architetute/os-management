package com.os.workshop.service.usecases;

import com.os.workshop.service.adapter.database.ServiceRepository;
import com.os.workshop.service.domain.ServiceEntity;
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
class FindServicesByIdOSUCTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private FindServicesByIdOSUC useCase;

    @Test
    void returnsServicesForOrderId() {
        UUID orderId = UUID.randomUUID();
        ServiceEntity service = new ServiceEntity();

        when(serviceRepository.findByIdOS(orderId)).thenReturn(List.of(service));

        assertEquals(1, useCase.process(orderId).size());
    }
}
