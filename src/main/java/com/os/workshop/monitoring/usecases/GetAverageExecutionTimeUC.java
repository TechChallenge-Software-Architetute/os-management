package com.os.workshop.monitoring.usecases;

import com.os.workshop.monitoring.domain.ServiceAverageTime;
import com.os.workshop.monitoring.domain.enums.AverageTimeEnum;
import com.os.workshop.monitoring.domain.requests.AverageExecutionTimeRequest;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetAverageExecutionTimeUC {

    private static final Logger logger = LoggerFactory.getLogger(GetAverageExecutionTimeUC.class);

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceAverageTime> process(AverageExecutionTimeRequest request) {
        logger.info("Calculando tempo médio de execução dos serviços...");

        List<ServiceEntity> services = serviceRepository.findAll();

        // Agrupar serviços por tipo
        Map<String, List<ServiceEntity>> groupedByType = services.stream()
                .collect(Collectors.groupingBy(service -> service.getServiceTypeName() != null ? service.getServiceTypeName() : "Unknown"));


        List<ServiceAverageTime> averages = groupedByType.entrySet().stream()
                .map(entry -> calculateAverageForType(entry, request.getTimeUnit()))
                .collect(Collectors.toList());

        logger.info("Calculado média pelos {} tipos de serviços", averages.size());
        return averages;
    }

    public ServiceAverageTime processById(UUID id, AverageTimeEnum request) {
        logger.info("Calculando tempo médio de execução dos serviço...");

        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado. ID: " + id));

        return service.getServiceTypeName() != null
                ? calculateAverageForType(Map.entry(service.getServiceTypeName(), List.of(service)), request)
                : new ServiceAverageTime("Unknown", 0.0);
    }

    private ServiceAverageTime calculateAverageForType(
            Map.Entry<String, List<ServiceEntity>> entry,
            AverageTimeEnum request
    ) {
        String type = entry.getKey();
        List<ServiceEntity> typeServices = entry.getValue();

        // Calcular tempo de execução para cada serviço concluído e filtrar tempos válidos
        List<Double> times = typeServices.stream()
                .filter(this::hasCompleted)
                .map(service -> calculateExecutionTime(service, request))
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

    private double calculateExecutionTime(ServiceEntity service, AverageTimeEnum request) {
        List<Status> status = service.getServiceStatus();

        LocalDateTime doingTime = findEarliestStatusTime(status, ServiceStatusEnum.DOING);
        LocalDateTime doneTime = findEarliestStatusTime(status, ServiceStatusEnum.DONE);

        if (doingTime != null && doneTime != null && doneTime.isAfter(doingTime)) {
            Duration duration = Duration.between(doingTime, doneTime);

            return request.calculate(duration);
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
