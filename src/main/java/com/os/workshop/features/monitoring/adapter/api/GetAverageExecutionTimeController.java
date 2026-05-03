package com.os.workshop.features.monitoring.adapter.api;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.monitoring.domain.ServiceAverageTime;
import com.os.workshop.features.monitoring.domain.requests.AverageExecutionTimeByIdRequest;
import com.os.workshop.features.monitoring.domain.requests.AverageExecutionTimeRequest;
import com.os.workshop.features.monitoring.usecases.GetAverageExecutionTimeUC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for monitoring service execution times.
 * Provides endpoints to calculate average execution times for services
 * grouped by service type or by individual service ID.
 */
@RestController
@RequestMapping("/monitoring")
@Tag(name = "Monitoring", description = "APIs for monitoring service execution times")
public class GetAverageExecutionTimeController {

    private static final Logger logger = LoggerFactory.getLogger(GetAverageExecutionTimeController.class);

    @Autowired
    private GetAverageExecutionTimeUC getAverageExecutionTimeUC;

    @Operation(
            summary = "Get average execution time of all services",
            description = "Calculates and returns the average execution time for services based on provided filters"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Average execution times calculated successfully",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ServiceAverageTime.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping({"/average-time", "/all"})
    public ResponseEntity<List<ServiceAverageTime>> getAverageExecutionTime(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Time unit used to calculate average execution time across service types.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AverageExecutionTimeRequest.class)))
            @Valid @RequestBody AverageExecutionTimeRequest request
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

    @Operation(
            summary = "Get average execution time by service ID",
            description = "Calculates and returns the average execution time for one service using the requested time unit."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Average execution time calculated successfully", content = @Content(schema = @Schema(implementation = ServiceAverageTime.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping({"/services/average-time", "/by-id"})
    public ResponseEntity<ServiceAverageTime> getAverageExecutionTimeById(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Service identifier and time unit used to calculate average execution time.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AverageExecutionTimeByIdRequest.class)))
            @Valid @RequestBody AverageExecutionTimeByIdRequest request
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
