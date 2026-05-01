package com.os.workshop.monitoring.adapter.api;

import com.os.workshop.monitoring.domain.ServiceAverageTime;
import com.os.workshop.monitoring.usecases.GetAverageExecutionTimeUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitoring")
public class GetAverageExecutionTimeController {

    private static final Logger logger = LoggerFactory.getLogger(GetAverageExecutionTimeController.class);

    @Autowired
    private GetAverageExecutionTimeUC getAverageExecutionTimeUC;

    @GetMapping("")
    public ResponseEntity<List<ServiceAverageTime>> getAverageExecutionTime() {
        logger.info("Recebida requisição para obter tempo médio de execução dos serviços.");

        try {
            List<ServiceAverageTime> averages = getAverageExecutionTimeUC.process();
            logger.info("Tempos médios calculados com sucesso. Total de tipos de serviço: {}", averages.size());
            return ResponseEntity.ok(averages);
        } catch (Exception e) {
            logger.error("Erro ao calcular tempos médios de execução: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
