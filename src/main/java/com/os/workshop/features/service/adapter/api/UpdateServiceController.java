package com.os.workshop.features.service.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.UpdateServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceUC;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Services", description = "Update services linked to service orders.")
@RestController
@RequestMapping("/services")
@AllArgsConstructor
public class UpdateServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateServiceController.class);

    private UpdateServiceUC updateServiceUC;

    @Operation(summary = "Update resource", description = "Update resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntity> update(
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(

                    description = "Request payload for this operation",

                    required = true,

                    content = @Content(schema = @Schema(implementation = UpdateServiceRequest.class))

            )

            @RequestBody UpdateServiceRequest request
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
