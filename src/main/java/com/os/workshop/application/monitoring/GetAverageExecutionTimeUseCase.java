package com.os.workshop.application.monitoring;

import com.os.workshop.domain.monitoring.AverageTimeEnum;
import com.os.workshop.domain.monitoring.ServiceAverageTime;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.infrastructure.persistence.service.ServiceEntity;
import com.os.workshop.infrastructure.persistence.service.ServiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAverageExecutionTimeUseCase {

    private final ServiceJpaRepository serviceRepository;

    public List<ServiceAverageTime> execute(AverageTimeEnum timeUnit) {
        List<ServiceEntity> services = serviceRepository.findAll();

        Map<String, List<ServiceEntity>> groupedByType = services.stream()
                .collect(Collectors.groupingBy(service ->
                        service.getServiceTypeName() != null ? service.getServiceTypeName() : "Unknown"));

        return groupedByType.entrySet().stream()
                .map(entry -> calculateAverageForType(entry, timeUnit))
                .toList();
    }

    private ServiceAverageTime calculateAverageForType(
            Map.Entry<String, List<ServiceEntity>> entry, AverageTimeEnum timeUnit) {
        String type = entry.getKey();
        List<Double> times = entry.getValue().stream()
                .filter(this::hasCompleted)
                .map(service -> calculateExecutionTime(service, timeUnit))
                .filter(time -> time > 0)
                .toList();

        double average = times.isEmpty() ? 0.0 : times.stream()
                .mapToDouble(Double::doubleValue).average().orElse(0.0);
        return new ServiceAverageTime(type, average);
    }

    private boolean hasCompleted(ServiceEntity service) {
        return service.getServiceStatus().stream()
                .anyMatch(status -> status.getStatus() == ServiceStatusEnum.DONE);
    }

    private double calculateExecutionTime(ServiceEntity service, AverageTimeEnum timeUnit) {
        List<Status> status = service.getServiceStatus();
        LocalDateTime doingTime = findEarliestStatusTime(status, ServiceStatusEnum.DOING);
        LocalDateTime doneTime = findEarliestStatusTime(status, ServiceStatusEnum.DONE);

        if (doingTime != null && doneTime != null && doneTime.isAfter(doingTime)) {
            return timeUnit.calculate(Duration.between(doingTime, doneTime));
        }
        return 0.0;
    }

    private LocalDateTime findEarliestStatusTime(List<Status> statuses, ServiceStatusEnum targetStatus) {
        return statuses.stream()
                .filter(status -> status.getStatus() == targetStatus)
                .map(Status::getChangedAt)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }
}
