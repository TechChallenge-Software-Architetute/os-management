package com.os.workshop.features.monitoring.averageExecutionTimeById;

import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.domain.Status;
import com.os.workshop.features.service.shared.domain.ServiceStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class GetAverageExecutionTimeByIdHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetAverageExecutionTimeByIdHandler.class);

    @Autowired
    private ServiceRepository serviceRepository;

    public ServiceAverageTime handle(UUID id, AverageTimeEnum timeUnit) {
        logger.info("Calculando tempo médio de execução do serviço...");

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

        if (!hasCompleted) {
            return 0.0;
        }

        LocalDateTime doingTime = findEarliestStatusTime(statuses, ServiceStatusEnum.DOING);
        LocalDateTime doneTime = findEarliestStatusTime(statuses, ServiceStatusEnum.DONE);

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
