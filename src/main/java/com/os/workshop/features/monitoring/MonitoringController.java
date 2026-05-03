package com.os.workshop.features.monitoring;

import com.os.workshop.features.monitoring.averageExecutionTime.GetAverageExecutionTimeHandler;
import com.os.workshop.features.monitoring.averageExecutionTime.GetAverageExecutionTimeRequest;
import com.os.workshop.features.monitoring.averageExecutionTimeById.GetAverageExecutionTimeByIdHandler;
import com.os.workshop.features.monitoring.averageExecutionTimeById.GetAverageExecutionTimeByIdRequest;
import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    private final GetAverageExecutionTimeHandler getAverageExecutionTimeHandler;
    private final GetAverageExecutionTimeByIdHandler getAverageExecutionTimeByIdHandler;

    @PostMapping("/all")
    public ResponseEntity<List<ServiceAverageTime>> getAverageExecutionTime(
            @RequestBody GetAverageExecutionTimeRequest request
    ) {
        logger.info("Recebida requisição para obter tempo médio de execução dos serviços.");

        try {
            List<ServiceAverageTime> averages = getAverageExecutionTimeHandler.handle(request);
            logger.info("Tempos médios calculados com sucesso. Total de tipos de serviço: {}", averages.size());
            return ResponseEntity.ok(averages);
        } catch (Exception e) {
            logger.error("Erro ao calcular tempos médios de execução: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/by-id")
    public ResponseEntity<ServiceAverageTime> getAverageExecutionTimeById(
            @RequestBody GetAverageExecutionTimeByIdRequest request
    ) {
        logger.info("Recebida requisição para obter tempo médio de execução do serviço.");

        try {
            ServiceAverageTime average = getAverageExecutionTimeByIdHandler.handle(
                    request.getId(), request.getTimeUnit());
            logger.info("Tempos médios calculados com sucesso.");
            return ResponseEntity.ok(average);
        } catch (Exception e) {
            logger.error("Erro ao calcular tempos médios de execução: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
