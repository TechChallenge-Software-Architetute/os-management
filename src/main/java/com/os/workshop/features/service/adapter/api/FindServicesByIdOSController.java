package com.os.workshop.features.service.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.FindServicesByIdOSUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Services", description = "Find services linked to a service order.")
@RestController
@RequestMapping("/services")
public class FindServicesByIdOSController {

    private static final Logger logger = LoggerFactory.getLogger(FindServicesByIdOSController.class);

    @Autowired
    private FindServicesByIdOSUC findServicesByIdOSUC;

    @Operation(summary = "Find services by service order", description = "Find services by service order endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/os/{idOS}")
    public ResponseEntity<List<ServiceEntity>> findByIdOS(@PathVariable UUID idOS) {
        logger.info("Recebida requisicao para buscar servicos por ID OS: {}", idOS);

        try {
            var services = findServicesByIdOSUC.process(idOS);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Erro ao buscar servicos por ID OS. ID OS: {}. Erro: {}", idOS, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
