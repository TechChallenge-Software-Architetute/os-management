package com.os.workshop.monitoring.usecases;

import com.os.workshop.features.monitoring.domain.ServiceAverageTime;
import com.os.workshop.features.monitoring.domain.enums.AverageTimeEnum;
import com.os.workshop.features.monitoring.domain.requests.AverageExecutionTimeRequest;
import com.os.workshop.features.monitoring.usecases.GetAverageExecutionTimeUC;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAverageExecutionTimeUCTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private GetAverageExecutionTimeUC getAverageExecutionTimeUC;

    private UUID serviceId1;
    private UUID serviceId2;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        serviceId1 = UUID.randomUUID();
        serviceId2 = UUID.randomUUID();
        baseTime = LocalDateTime.now();
    }

    // Helper methods to create test data
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
    void process_whenNoServices_thenReturnEmptyList() {
        // Arrange
        when(serviceRepository.findAll()).thenReturn(List.of());
        AverageExecutionTimeRequest request = new AverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.SECONDS);

        // Act
        List<ServiceAverageTime> result = getAverageExecutionTimeUC.process(request);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void process_whenServicesWithSameTypeAndCompleted_thenReturnAverage() {
        // Arrange
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
        AverageExecutionTimeRequest request = new AverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.SECONDS);

        // Act
        List<ServiceAverageTime> result = getAverageExecutionTimeUC.process(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("TypeA", result.get(0).getServiceTypeName());
        assertEquals(15.0, result.get(0).getAverageTime());
    }

    @Test
    void process_whenServicesWithDifferentTypes_thenReturnMultipleAverages() {
        // Arrange
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
        AverageExecutionTimeRequest request = new AverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.SECONDS);

        // Act
        List<ServiceAverageTime> result = getAverageExecutionTimeUC.process(request);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getServiceTypeName().equals("TypeA") && s.getAverageTime() == 10.0));
        assertTrue(result.stream().anyMatch(s -> s.getServiceTypeName().equals("TypeB") && s.getAverageTime() == 60.0));
    }

    @Test
    void process_whenServiceNotCompleted_thenExcludeFromAverage() {
        // Arrange
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        List<Status> statuses2 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime)
        ); // Not DONE
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        ServiceEntity service2 = createServiceEntity(serviceId2, "TypeA", statuses2);
        when(serviceRepository.findAll()).thenReturn(List.of(service1, service2));
        AverageExecutionTimeRequest request = new AverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.SECONDS);

        // Act
        List<ServiceAverageTime> result = getAverageExecutionTimeUC.process(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("TypeA", result.get(0).getServiceTypeName());
        assertEquals(10.0, result.get(0).getAverageTime());
    }

    @Test
    void process_whenServiceHasInvalidTime_thenExcludeFromAverage() {
        // Arrange
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.minusSeconds(10)) // DONE before DOING
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        when(serviceRepository.findAll()).thenReturn(List.of(service1));
        AverageExecutionTimeRequest request = new AverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.SECONDS);

        // Act
        List<ServiceAverageTime> result = getAverageExecutionTimeUC.process(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("TypeA", result.get(0).getServiceTypeName());
        assertEquals(0.0, result.get(0).getAverageTime());
    }

    @Test
    void process_whenServiceTypeNameIsNull_thenGroupAsUnknown() {
        // Arrange
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, null, statuses1);
        when(serviceRepository.findAll()).thenReturn(List.of(service1));
        AverageExecutionTimeRequest request = new AverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.SECONDS);

        // Act
        List<ServiceAverageTime> result = getAverageExecutionTimeUC.process(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).getServiceTypeName());  // Updated expectation
        assertEquals(10.0, result.get(0).getAverageTime());
    }


    @Test
    void process_whenDifferentTimeUnits_thenCalculateCorrectly() {
        // Arrange
        List<Status> statuses1 = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(60)) // 1 minute
        );
        ServiceEntity service1 = createServiceEntity(serviceId1, "TypeA", statuses1);
        when(serviceRepository.findAll()).thenReturn(List.of(service1));

        // Test SECONDS
        AverageExecutionTimeRequest requestSeconds = new AverageExecutionTimeRequest();
        requestSeconds.setTimeUnit(AverageTimeEnum.SECONDS);
        List<ServiceAverageTime> resultSeconds = getAverageExecutionTimeUC.process(requestSeconds);
        assertEquals(60.0, resultSeconds.get(0).getAverageTime());

        // Test MINUTES
        AverageExecutionTimeRequest requestMinutes = new AverageExecutionTimeRequest();
        requestMinutes.setTimeUnit(AverageTimeEnum.MINUTES);
        List<ServiceAverageTime> resultMinutes = getAverageExecutionTimeUC.process(requestMinutes);
        assertEquals(1.0, resultMinutes.get(0).getAverageTime());

        // Test HOURS
        AverageExecutionTimeRequest requestHours = new AverageExecutionTimeRequest();
        requestHours.setTimeUnit(AverageTimeEnum.HOURS);
        List<ServiceAverageTime> resultHours = getAverageExecutionTimeUC.process(requestHours);
        assertEquals(1.0 / 60.0, resultHours.get(0).getAverageTime());
    }

    @Test
    void processById_whenServiceFoundWithType_thenReturnAverage() {
        // Arrange
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        ServiceEntity service = createServiceEntity(serviceId1, "TypeA", statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        // Act
        ServiceAverageTime result = getAverageExecutionTimeUC.processById(serviceId1, AverageTimeEnum.SECONDS);

        // Assert
        assertEquals("TypeA", result.getServiceTypeName());
        assertEquals(10.0, result.getAverageTime());
    }

    @Test
    void processById_whenServiceFoundWithNullType_thenReturnUnknown() {
        // Arrange
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.plusSeconds(10))
        );
        ServiceEntity service = createServiceEntity(serviceId1, null, statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        // Act
        ServiceAverageTime result = getAverageExecutionTimeUC.processById(serviceId1, AverageTimeEnum.SECONDS);

        // Assert
        assertEquals("Unknown", result.getServiceTypeName());
        assertEquals(0.0, result.getAverageTime());
    }

    @Test
    void processById_whenServiceNotFound_thenThrowRuntimeException() {
        // Arrange
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                getAverageExecutionTimeUC.processById(serviceId1, AverageTimeEnum.SECONDS));
        assertEquals("Servico nao encontrado. ID: " + serviceId1, exception.getMessage());
    }

    @Test
    void processById_whenServiceNotCompleted_thenReturnZero() {
        // Arrange
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime)
        );
        ServiceEntity service = createServiceEntity(serviceId1, "TypeA", statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        // Act
        ServiceAverageTime result = getAverageExecutionTimeUC.processById(serviceId1, AverageTimeEnum.SECONDS);

        // Assert
        assertEquals("TypeA", result.getServiceTypeName());
        assertEquals(0.0, result.getAverageTime());
    }

    @Test
    void processById_whenServiceHasInvalidTime_thenReturnZero() {
        // Arrange
        List<Status> statuses = List.of(
                createStatus(ServiceStatusEnum.DOING, baseTime),
                createStatus(ServiceStatusEnum.DONE, baseTime.minusSeconds(10))
        );
        ServiceEntity service = createServiceEntity(serviceId1, "TypeA", statuses);
        when(serviceRepository.findById(serviceId1)).thenReturn(Optional.of(service));

        // Act
        ServiceAverageTime result = getAverageExecutionTimeUC.processById(serviceId1, AverageTimeEnum.SECONDS);

        // Assert
        assertEquals("TypeA", result.getServiceTypeName());
        assertEquals(0.0, result.getAverageTime());
    }
}
