package com.os.workshop.features.monitoring.averageExecutionTime;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.infrastructure.persistence.service.ServiceEntity;
import com.os.workshop.infrastructure.persistence.service.ServiceJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetAverageExecutionTimeHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetAverageExecutionTimeHandler.class);

    @Autowired
    private ServiceJpaRepository serviceRepository;

    public List<ServiceAverageTime> handle(GetAverageExecutionTimeRequest request) {
        logger.info("Calculando tempo médio de execução dos serviços...");

        List<ServiceEntity> services = serviceRepository.findAll();

        Map<String, List<ServiceEntity>> groupedByType = services.stream()
                .collect(Collectors.groupingBy(service ->
                        service.getServiceTypeName() != null ? service.getServiceTypeName() : "Unknown"));

        List<ServiceAverageTime> averages = groupedByType.entrySet().stream()
                .map(entry -> calculateAverageForType(entry, request.getTimeUnit()))
                .toList();

        logger.info("Calculado média pelos {} tipos de serviços", averages.size());
        return averages;
    }

    private ServiceAverageTime calculateAverageForType(
            Map.Entry<String, List<ServiceEntity>> entry,
            AverageTimeEnum timeUnit
    ) {
        String type = entry.getKey();
        List<ServiceEntity> typeServices = entry.getValue();

        List<Double> times = typeServices.stream()
                .filter(this::hasCompleted)
                .map(service -> calculateExecutionTime(service, timeUnit))
                .filter(time -> time > 0)
                .toList();

        double average = times.isEmpty() ? 0.0 : times.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

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
            Duration duration = Duration.between(doingTime, doneTime);
            return timeUnit.calculate(duration);
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
