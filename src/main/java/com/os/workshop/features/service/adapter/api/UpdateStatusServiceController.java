package com.os.workshop.features.service.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.UpdateStatusServiceRequest;
import com.os.workshop.features.service.usecases.UpdateServiceStatusUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Services", description = "Update service execution status.")
@RestController
@RequestMapping("/services")
public class UpdateStatusServiceController {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStatusServiceController.class);

    @Autowired
    private UpdateServiceStatusUC updateServiceStatusUC;

    @Operation(summary = "List resources", description = "List resources endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/update-status")
    public ResponseEntity<ServiceEntity> listServices(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(

                    description = "Request payload for this operation",

                    required = true,

                    content = @Content(schema = @Schema(implementation = UpdateStatusServiceRequest.class))

            )

            @RequestBody UpdateStatusServiceRequest request
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
