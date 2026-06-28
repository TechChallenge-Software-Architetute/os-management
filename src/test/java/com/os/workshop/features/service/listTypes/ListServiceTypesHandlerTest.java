package com.os.workshop.features.service.listTypes;

import com.os.workshop.application.service.ListServiceTypesUseCase;
import com.os.workshop.application.service.port.out.ServiceTypeRepository;
import com.os.workshop.domain.service.ServiceType;
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
class ListServiceTypesHandlerTest {

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    @InjectMocks
    private ListServiceTypesUseCase useCase;

    @Test
    void returnsAllServiceTypes() {
        when(serviceTypeRepository.findAll())
                .thenReturn(List.of(new ServiceType(UUID.randomUUID(), "REVISAO", "Revisao")));
        assertEquals(1, useCase.execute().size());
    }

    @Test
    void propagatesRepositoryException() {
        when(serviceTypeRepository.findAll()).thenThrow(new RuntimeException("failure"));
        assertThrows(RuntimeException.class, () -> useCase.execute());
    }
}
