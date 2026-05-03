package com.os.workshop.features.service.usecases;

import com.os.workshop.features.service.adapter.database.ServiceTypeRepository;
import com.os.workshop.features.service.domain.ServiceTypeEntity;
import com.os.workshop.features.service.usecases.ListServiceTypeUC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListServiceTypeUCTest {

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @InjectMocks
    private ListServiceTypeUC useCase;

    @Test
    void returnsAllServiceTypes() {
        when(serviceTypeRepository.findAll())
                .thenReturn(List.of(new ServiceTypeEntity(UUID.randomUUID(), "REVISAO", "Revisao")));

        assertEquals(1, useCase.process().size());
    }

    @Test
    void propagatesRepositoryException() {
        when(serviceTypeRepository.findAll()).thenThrow(new RuntimeException("failure"));

        assertThrows(RuntimeException.class, () -> useCase.process());
    }
}
