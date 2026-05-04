package com.os.workshop.features.service.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.ListServicesUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Services", description = "List services.")
@RestController
@RequestMapping("/services")
public class ListServicesController {

    private static final Logger logger = LoggerFactory.getLogger(ListServicesController.class);

    @Autowired
    private ListServicesUC listServicesUC;

    @Operation(summary = "List resources", description = "List resources endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ServiceEntity>> listServices() {
        logger.info("Recebida requisição para listar serviços.");

        try {

            List<ServiceEntity> services = listServicesUC.process();

            logger.info("Serviços listados com sucesso. Serviços: {}", services);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
