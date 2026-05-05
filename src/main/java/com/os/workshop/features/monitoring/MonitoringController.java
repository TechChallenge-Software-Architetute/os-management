package com.os.workshop.features.monitoring;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

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

@Tag(name = "Monitoring", description = "Read service execution time metrics.")
@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    private final GetAverageExecutionTimeHandler getAverageExecutionTimeHandler;
    private final GetAverageExecutionTimeByIdHandler getAverageExecutionTimeByIdHandler;

    @Operation(summary = "Get average execution time", description = "Get average execution time endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/all")
    public ResponseEntity<List<ServiceAverageTime>> getAverageExecutionTime(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(

                    description = "Request payload for this operation",

                    required = true,

                    content = @Content(schema = @Schema(implementation = GetAverageExecutionTimeRequest.class))

            )

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

    @Operation(summary = "Get average execution time by id", description = "Get average execution time by id endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/by-id")
    public ResponseEntity<ServiceAverageTime> getAverageExecutionTimeById(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(

                    description = "Request payload for this operation",

                    required = true,

                    content = @Content(schema = @Schema(implementation = GetAverageExecutionTimeByIdRequest.class))

            )

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
