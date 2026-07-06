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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAverageExecutionTimeByIdUseCase {

    private final ServiceJpaRepository serviceRepository;

    public ServiceAverageTime execute(UUID id, AverageTimeEnum timeUnit) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado. ID: " + id));

        if (service.getServiceTypeName() == null) {
            return new ServiceAverageTime("Unknown", 0.0);
        }

        double executionTime = calculateExecutionTime(service, timeUnit);
        return new ServiceAverageTime(service.getServiceTypeName(), executionTime);
    }

    private double calculateExecutionTime(ServiceEntity service, AverageTimeEnum timeUnit) {
        List<Status> statuses = service.getServiceStatus();
        boolean hasCompleted = statuses.stream()
                .anyMatch(status -> status.getStatus() == ServiceStatusEnum.DONE);
        if (!hasCompleted) return 0.0;

        LocalDateTime doingTime = findEarliestStatusTime(statuses, ServiceStatusEnum.DOING);
        LocalDateTime doneTime = findEarliestStatusTime(statuses, ServiceStatusEnum.DONE);

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
