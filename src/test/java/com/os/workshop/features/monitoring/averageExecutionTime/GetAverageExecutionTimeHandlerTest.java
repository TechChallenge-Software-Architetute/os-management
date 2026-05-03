package com.os.workshop.features.monitoring.averageExecutionTime;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;
import com.os.workshop.features.service.adapter.database.ServiceRepository;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.Status;
import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAverageExecutionTimeHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private GetAverageExecutionTimeHandler getAverageExecutionTimeHandler;

    private UUID serviceId1;
    private UUID serviceId2;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        serviceId1 = UUID.randomUUID();
        serviceId2 = UUID.randomUUID();
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

    private GetAverageExecutionTimeRequest createRequest(AverageTimeEnum timeUnit) {
        GetAverageExecutionTimeRequest request = new GetAverageExecutionTimeRequest();
        request.setTimeUnit(timeUnit);
        return request;
    }

    @Test
    void handle_whenNoServices_thenReturnEmptyList() {
        when(serviceRepository.findAll()).thenReturn(List.of());

        List<ServiceAverageTime> result = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.SECONDS));

        assertTrue(result.isEmpty());
    }

    @Test
    void handle_whenServicesWithSameTypeAndCompleted_thenReturnAverage() {
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        List<Status> statuses2 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(20))
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        ServiceEntity service2 = createServiceEntity(serviceId2, "TypeA", statuses2);
        when(serviceRepository.findAll()).thenReturn(List.of(service1, service2));

        List<ServiceAverageTime> result = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.SECONDS));

        assertEquals(1, result.size());
        assertEquals("TypeA", result.get(0).getServiceTypeName());
        assertEquals(15.0, result.get(0).getAverageTime());
    }

    @Test
    void handle_whenServicesWithDifferentTypes_thenReturnMultipleAverages() {
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        List<Status> statuses2 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusMinutes(1))
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        ServiceEntity service2 = createServiceEntity(serviceId2, "TypeB", statuses2);
        when(serviceRepository.findAll()).thenReturn(List.of(service1, service2));

        List<ServiceAverageTime> result = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.SECONDS));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getServiceTypeName().equals("TypeA") && s.getAverageTime() == 10.0));
        assertTrue(result.stream().anyMatch(s -> s.getServiceTypeName().equals("TypeB") && s.getAverageTime() == 60.0));
    }

    @Test
    void handle_whenServiceNotCompleted_thenExcludeFromAverage() {
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        List<Status> statuses2 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime)
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        ServiceEntity service2 = createServiceEntity(serviceId2, "TypeA", statuses2);
        when(serviceRepository.findAll()).thenReturn(List.of(service1, service2));

        List<ServiceAverageTime> result = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.SECONDS));

        assertEquals(1, result.size());
        assertEquals("TypeA", result.get(0).getServiceTypeName());
        assertEquals(10.0, result.get(0).getAverageTime());
    }

    @Test
    void handle_whenServiceHasInvalidTime_thenExcludeFromAverage() {
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.minusSeconds(10))
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        when(serviceRepository.findAll()).thenReturn(List.of(service1));

        List<ServiceAverageTime> result = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.SECONDS));

        assertEquals(1, result.size());
        assertEquals("TypeA", result.get(0).getServiceTypeName());
        assertEquals(0.0, result.get(0).getAverageTime());
    }

    @Test
    void handle_whenServiceTypeNameIsNull_thenGroupAsUnknown() {
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, null, statuses1);
        when(serviceRepository.findAll()).thenReturn(List.of(service1));

        List<ServiceAverageTime> result = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.SECONDS));

        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).getServiceTypeName());
        assertEquals(10.0, result.get(0).getAverageTime());
    }

    @Test
    void handle_whenDifferentTimeUnits_thenCalculateCorrectly() {
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(60))
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        when(serviceRepository.findAll()).thenReturn(List.of(service1));

        List<ServiceAverageTime> resultSeconds = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.SECONDS));
        assertEquals(60.0, resultSeconds.get(0).getAverageTime());

        List<ServiceAverageTime> resultMinutes = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.MINUTES));
        assertEquals(1.0, resultMinutes.get(0).getAverageTime());

        List<ServiceAverageTime> resultHours = getAverageExecutionTimeHandler.handle(
                createRequest(AverageTimeEnum.HOURS));
        assertEquals(1.0 / 60.0, resultHours.get(0).getAverageTime());
    }
}
