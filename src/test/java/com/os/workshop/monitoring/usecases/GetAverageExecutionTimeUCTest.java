package com.os.workshop.monitoring.usecases;

import com.os.workshop.monitoring.domain.enums.AverageTimeEnum;
import com.os.workshop.monitoring.domain.requests.AverageExecutionTimeRequest;
import com.os.workshop.service.adapter.database.ServiceRepository;
import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.domain.Status;
import com.os.workshop.service.domain.enums.ServiceStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAverageExecutionTimeUCTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private GetAverageExecutionTimeUC useCase;

    @Test
    void calculatesAverageByServiceType() {
        ServiceEntity service = service("REVISAO");
        AverageExecutionTimeRequest request = new AverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.MINUTES);

        when(serviceRepository.findAll()).thenReturn(List.of(service));

        var result = useCase.process(request);

        assertEquals(1, result.size());
        assertEquals("REVISAO", result.get(0).getServiceTypeName());
        assertEquals(10.0, result.get(0).getAverageTimeSeconds(), 0.1);
    }

    @Test
    void calculatesAverageById() {
        UUID id = UUID.randomUUID();
        ServiceEntity service = service("REVISAO");

        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));

        assertEquals("REVISAO", useCase.processById(id, AverageTimeEnum.SECONDS).getServiceTypeName());
    }

    @Test
    void throwsWhenServiceDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(serviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> useCase.processById(id, AverageTimeEnum.SECONDS));
    }

    private ServiceEntity service(String type) {
        ServiceEntity service = new ServiceEntity();
        service.setId(UUID.randomUUID());
        service.setServiceTypeName(type);
        service.setIdOS(UUID.randomUUID());
        service.setServiceStatus(List.of(
                new Status(ServiceStatusEnum.DOING, LocalDateTime.now().minusMinutes(10)),
                new Status(ServiceStatusEnum.DONE, LocalDateTime.now())
        ));
        return service;
    }
}
