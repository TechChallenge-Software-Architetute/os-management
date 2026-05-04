package com.os.workshop.features.service.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.FindServiceByIdUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Services", description = "Find services by identifier.")
@RestController
@RequestMapping("/services")
public class FindServiceByIdController {

    private static final Logger logger = LoggerFactory.getLogger(FindServiceByIdController.class);

    @Autowired
    private FindServiceByIdUC findServiceByIdUC;

    @Operation(summary = "Find resource by id", description = "Find resource by id endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntity> findById(@PathVariable UUID id) {
        logger.info("Recebida requisicao para buscar servico por ID: {}", id);

        try {
            var service = findServiceByIdUC.process(id);

            logger.info("Servico encontrado. Servico: {}", service);

            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            logger.error("Servico nao encontrado. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Erro ao buscar servico por ID. ID: {}. Erro: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
