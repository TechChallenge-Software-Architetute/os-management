package com.os.workshop.monitoring.usecases;

import com.os.workshop.monitoring.domain.ServiceAverageTime;
import com.os.workshop.service.adapter.database.ServiceRepository;
import com.os.workshop.service.domain.ServiceEntity;
import com.os.workshop.service.domain.Status;
import com.os.workshop.service.domain.enums.ServiceStatusEnum;
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
public class GetAverageExecutionTimeUC {

    private static final Logger logger = LoggerFactory.getLogger(GetAverageExecutionTimeUC.class);

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceAverageTime> process() {
        logger.info("Calculando tempo médio de execução dos serviços...");

        List<ServiceEntity> services = serviceRepository.findAll();

        // Agrupar serviços por tipo
        Map<String, List<ServiceEntity>> groupedByType = services.stream()
                .collect(Collectors.groupingBy(ServiceEntity::getServiceTypeName));

        List<ServiceAverageTime> averages = groupedByType.entrySet().stream()
                .map(this::calculateAverageForType)
                .collect(Collectors.toList());

        logger.info("Calculado média pelos {} tipos de serviços", averages.size());
        return averages;
    }

    private ServiceAverageTime calculateAverageForType(Map.Entry<String, List<ServiceEntity>> entry) {
        String type = entry.getKey();
        List<ServiceEntity> typeServices = entry.getValue();

        // Calcular tempo de execução para cada serviço concluído e filtrar tempos válidos
        List<Double> times = typeServices.stream()
                .filter(this::hasCompleted)
                .map(this::calculateExecutionTime)
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

    private double calculateExecutionTime(ServiceEntity service) {
        List<Status> statuses = service.getServiceStatus();

        LocalDateTime doingTime = findEarliestStatusTime(statuses, ServiceStatusEnum.DOING);
        LocalDateTime doneTime = findEarliestStatusTime(statuses, ServiceStatusEnum.DONE);

        if (doingTime != null && doneTime != null && doneTime.isAfter(doingTime)) {
            long minutes = Duration.between(doingTime, doneTime).toMinutes();
            return (double) minutes;
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
