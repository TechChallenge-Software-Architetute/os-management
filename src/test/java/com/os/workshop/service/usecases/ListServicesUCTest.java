package com.os.workshop.service.usecases;

import com.os.workshop.features.service.adapter.database.ServiceRepository;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.ListServicesUC;
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
class ListServicesUCTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ListServicesUC useCase;

    @Test
    void returnsAllServices() {
        when(serviceRepository.findAll()).thenReturn(List.of(new ServiceEntity()));

        assertEquals(1, useCase.process().size());
    }

    @Test
    void propagatesRepositoryException() {
        when(serviceRepository.findAll()).thenThrow(new RuntimeException("failure"));

        assertThrows(RuntimeException.class, () -> useCase.process());
    }
}
