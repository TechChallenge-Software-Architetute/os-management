package com.os.workshop.features.monitoring.adapter.api;

import com.os.workshop.features.monitoring.domain.ServiceAverageTime;
import com.os.workshop.features.monitoring.domain.requests.AverageExecutionTimeByIdRequest;
import com.os.workshop.features.monitoring.domain.requests.AverageExecutionTimeRequest;
import com.os.workshop.features.monitoring.usecases.GetAverageExecutionTimeUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitoring")
public class GetAverageExecutionTimeController {

    private static final Logger logger = LoggerFactory.getLogger(GetAverageExecutionTimeController.class);

    @Autowired
    private GetAverageExecutionTimeUC getAverageExecutionTimeUC;

    @PostMapping("/all")
    public ResponseEntity<List<ServiceAverageTime>> getAverageExecutionTime(
            @RequestBody AverageExecutionTimeRequest request
    ) {
        logger.info("Recebida requisição para obter tempo médio de execução dos serviços.");

        try {
            List<ServiceAverageTime> averages = getAverageExecutionTimeUC.process(request);
            logger.info("Tempos médios calculados com sucesso. Total de tipos de serviço: {}", averages.size());
            return ResponseEntity.ok(averages);
        } catch (Exception e) {
            logger.error("Erro ao calcular tempos médios de execução: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/by-id")
    public ResponseEntity<ServiceAverageTime> getAverageExecutionTimeById(
            @RequestBody AverageExecutionTimeByIdRequest request
    ) {
        logger.info("Recebida requisição para obter tempo médio de execução do serviço.");

        try {
            ServiceAverageTime average = getAverageExecutionTimeUC.processById(request.getId(), request.getTimeUnit());
            logger.info("Tempos médios calculados com sucesso.");
            return ResponseEntity.ok(average);
        } catch (Exception e) {
            logger.error("Erro ao calcular tempos médios de execução: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
