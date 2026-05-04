package com.os.workshop.features.service.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.service.domain.ServiceTypeEntity;
import com.os.workshop.features.service.usecases.ListServiceTypeUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Service Types", description = "List available service types.")
@RestController
@RequestMapping("/service-types")
public class ListServiceTypeController {

    private static final Logger logger = LoggerFactory.getLogger(ListServiceTypeController.class);

    @Autowired
    private ListServiceTypeUC listServiceTypeUC;

    @Operation(summary = "List resources", description = "List resources endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ServiceTypeEntity>> listServices() {
        logger.info("Recebida requisição para listar serviços.");

        try {

            List<ServiceTypeEntity> serviceType = listServiceTypeUC.process();

            logger.info("Tipos de Serviços listados com sucesso. Tipos: {}", serviceType);

            return ResponseEntity.ok(serviceType);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
