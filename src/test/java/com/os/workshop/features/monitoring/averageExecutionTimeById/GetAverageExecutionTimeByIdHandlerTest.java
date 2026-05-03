package com.os.workshop.features.monitoring.averageExecutionTimeById;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.domain.Status;
import com.os.workshop.features.service.shared.domain.ServiceStatusEnum;
import org.junit.jupiter.api.BeforeEach;
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
class GetAverageExecutionTimeByIdHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private GetAverageExecutionTimeByIdHandler getAverageExecutionTimeByIdHandler;

    private UUID serviceId1;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        serviceId1 = UUID.randomUUID();
        baseTime = LocalDateTime.now();
    }

    private ServiceEntity createServiceEntity(UUID id, String typeName, List<Status> statuses) {
        ServiceEntity entity = new ServiceEntity();
        entity.setId(id);
        entity.setServiceTypeName(typeName);
        entity.setIdOS(UUID.randomUUID());
        entity.setServiceStatus(statuses);
        return entity;
    }

    private Status createStatus(ServiceStatusEnum status, LocalDateTime changedAt) {
        return new Status(status, changedAt);
    }

    @Test
    void handle_whenServiceFoundWithType_thenReturnAverage() {
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        ServiceEntity service = createServiceEntity(serviceId1, "TypeA", statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        ServiceAverageTime result = getAverageExecutionTimeByIdHandler.handle(serviceId1, AverageTimeEnum.SECONDS);

        assertEquals("TypeA", result.getServiceTypeName());
        assertEquals(10.0, result.getAverageTime());
    }

    @Test
    void handle_whenServiceFoundWithNullType_thenReturnUnknown() {
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        ServiceEntity service = createServiceEntity(serviceId1, null, statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        ServiceAverageTime result = getAverageExecutionTimeByIdHandler.handle(serviceId1, AverageTimeEnum.SECONDS);

        assertEquals("Unknown", result.getServiceTypeName());
        assertEquals(0.0, result.getAverageTime());
    }

    @Test
    void handle_whenServiceNotFound_thenThrowRuntimeException() {
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                getAverageExecutionTimeByIdHandler.handle(serviceId1, AverageTimeEnum.SECONDS));
        assertEquals("Servico nao encontrado. ID: " + serviceId1, exception.getMessage());
    }

    @Test
    void handle_whenServiceNotCompleted_thenReturnZero() {
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime)
        );
        ServiceEntity service = createServiceEntity(serviceId1, "TypeA", statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        ServiceAverageTime result = getAverageExecutionTimeByIdHandler.handle(serviceId1, AverageTimeEnum.SECONDS);

        assertEquals("TypeA", result.getServiceTypeName());
        assertEquals(0.0, result.getAverageTime());
    }

    @Test
    void handle_whenServiceHasInvalidTime_thenReturnZero() {
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.minusSeconds(10))
        );
        ServiceEntity service = createServiceEntity(serviceId1, "TypeA", statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        ServiceAverageTime result = getAverageExecutionTimeByIdHandler.handle(serviceId1, AverageTimeEnum.SECONDS);

        assertEquals("TypeA", result.getServiceTypeName());
        assertEquals(0.0, result.getAverageTime());
    }
}
