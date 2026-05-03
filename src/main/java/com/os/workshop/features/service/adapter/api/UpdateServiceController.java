package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.UpdateServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceUC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/services")
@Tag(name = "Services", description = "Manage workshop services associated with service orders.")
public class UpdateServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateServiceController.class);

    @Autowired
    private UpdateServiceUC updateServiceUC;

    @PutMapping("/{id}")
    @Operation(summary = "Update service", description = "Updates the type and service-order association of an existing service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service updated successfully", content = @Content(schema = @Schema(implementation = ServiceEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceEntity> update(
            @Parameter(description = "Service unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Service data to update.", required = true, content = @Content(schema = @Schema(implementation = UpdateServiceRequest.class)))
            @Valid @RequestBody UpdateServiceRequest request
    ) {
        logger.info("Recebida requisicao para atualizar servico. ID: {}", id);

        try {
            var service = updateServiceUC.process(id, request);

            logger.info("Servico atualizado. Servico: {}", service);

            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar servico. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar servico. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
