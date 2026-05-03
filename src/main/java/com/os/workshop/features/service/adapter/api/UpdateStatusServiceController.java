package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.UpdateStatusServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceStatusUC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
@Tag(name = "Services", description = "Manage workshop services associated with service orders.")
public class UpdateStatusServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusServiceController.class);

    @Autowired
    private UpdateServiceStatusUC updateServiceStatusUC;

    @PatchMapping({"/status", "/update-status"})
    @Operation(summary = "Update service status", description = "Updates the lifecycle status of a service. The /update-status path is kept as a backward-compatible alias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service status updated successfully", content = @Content(schema = @Schema(implementation = ServiceEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status update request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceEntity> listServices(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Service identifier and status to apply.", required = true, content = @Content(schema = @Schema(implementation = UpdateStatusServiceRequest.class)))
            @Valid @RequestBody UpdateStatusServiceRequest request
    ) {
        logger.info("Recebida requisição para atualizar status serviços.");

        try {

            var service = updateServiceStatusUC.process(request);

            logger.info("Serviço atualizado. Serviço: {}", service);

            return ResponseEntity.ok().body(service);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
