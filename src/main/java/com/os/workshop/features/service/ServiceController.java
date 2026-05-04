package com.os.workshop.features.service;

import com.os.workshop.features.service.create.CreateServiceHandler;
import com.os.workshop.features.service.create.CreateServiceRequest;
import com.os.workshop.features.service.findById.FindServiceByIdHandler;
import com.os.workshop.features.service.findByServiceOrder.FindServicesByServiceOrderHandler;
import com.os.workshop.features.service.list.ListServicesHandler;
import com.os.workshop.features.service.listTypes.ListServiceTypesHandler;
import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceTypeEntity;
import com.os.workshop.features.service.update.UpdateServiceHandler;
import com.os.workshop.features.service.update.UpdateServiceRequest;
import com.os.workshop.features.service.updateStatus.UpdateServiceStatusHandler;
import com.os.workshop.features.service.updateStatus.UpdateServiceStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Services", description = "Manage services linked to service orders.")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private final CreateServiceHandler createServiceHandler;
    private final FindServiceByIdHandler findServiceByIdHandler;
    private final FindServicesByServiceOrderHandler findServicesByServiceOrderHandler;
    private final ListServicesHandler listServicesHandler;
    private final ListServiceTypesHandler listServiceTypesHandler;
    private final UpdateServiceHandler updateServiceHandler;
    private final UpdateServiceStatusHandler updateServiceStatusHandler;

    @Operation(summary = "Create service", description = "Creates a service linked to a service order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/services")
    public ResponseEntity<ServiceEntity> createService(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Service data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateServiceRequest.class))
            )
            @RequestBody CreateServiceRequest request) {
        try {
            var createdService = createServiceHandler.handle(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Find service by ID", description = "Returns a service by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceEntity> findById(@PathVariable UUID id) {
        logger.info("Recebida requisicao para buscar servico por ID: {}", id);
        try {
            var service = findServiceByIdHandler.handle(id);
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

    @Operation(summary = "Find services by service order", description = "Lists services linked to a service order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/services/os/{idOS}")
    public ResponseEntity<List<ServiceEntity>> findByIdOS(@PathVariable UUID idOS) {
        logger.info("Recebida requisicao para buscar servicos por ID OS: {}", idOS);
        try {
            var services = findServicesByServiceOrderHandler.handle(idOS);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Erro ao buscar servicos por ID OS. ID OS: {}. Erro: {}", idOS, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "List services", description = "Lists all services.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/services")
    public ResponseEntity<List<ServiceEntity>> listServices() {
        logger.info("Recebida requisição para listar serviços.");
        try {
            List<ServiceEntity> services = listServicesHandler.handle();
            logger.info("Serviços listados com sucesso. Serviços: {}", services);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "List service types", description = "Lists available service types.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service types listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/service-types")
    public ResponseEntity<List<ServiceTypeEntity>> listServiceTypes() {
        logger.info("Recebida requisição para listar serviços.");
        try {
            List<ServiceTypeEntity> serviceType = listServiceTypesHandler.handle();
            logger.info("Tipos de Serviços listados com sucesso. Tipos: {}", serviceType);
            return ResponseEntity.ok(serviceType);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update service", description = "Updates service data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceEntity> update(@PathVariable UUID id,
                                                @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                        description = "Updated service data.",
                                                        required = true,
                                                        content = @Content(schema = @Schema(implementation = UpdateServiceRequest.class))
                                                )
                                                @RequestBody UpdateServiceRequest request) {
        logger.info("Recebida requisicao para atualizar servico. ID: {}", id);
        try {
            var service = updateServiceHandler.handle(id, request);
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

    @Operation(summary = "Update service status", description = "Updates the status of a service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/services/update-status")
    public ResponseEntity<ServiceEntity> updateStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Service status data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateServiceStatusRequest.class))
            )
            @RequestBody UpdateServiceStatusRequest request) {
        logger.info("Recebida requisição para atualizar status serviços.");
        try {
            var service = updateServiceStatusHandler.handle(request);
            logger.info("Serviço atualizado. Serviço: {}", service);
            return ResponseEntity.ok().body(service);
        } catch (Exception e) {
            logger.error("Erro ao listar serviços. Erro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
