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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Services", description = "Manage services linked to service orders.")
public class ServiceController {

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
                    content = @Content(
                            schema = @Schema(implementation = CreateServiceRequest.class),
                            examples = @ExampleObject(value = "{\"ServiceType\":\"TROCA_OLEO\",\"idOS\":\"b46ac51b-5ca6-439b-ba52-a36bd52e8648\"}")
                    )
            )
            @Valid @RequestBody CreateServiceRequest request) {
        var createdService = createServiceHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }

    @Operation(summary = "Find service by ID", description = "Returns a service by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceEntity> findById(@PathVariable UUID id) {
        var service = findServiceByIdHandler.handle(id);
        return ResponseEntity.ok(service);
    }

    @Operation(summary = "Find services by service order", description = "Lists services linked to a service order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/services/os/{idOS}")
    public ResponseEntity<List<ServiceEntity>> findByIdOS(@PathVariable UUID idOS) {
        var services = findServicesByServiceOrderHandler.handle(idOS);
        return ResponseEntity.ok(services);
    }

    @Operation(summary = "List services", description = "Lists all services.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/services")
    public ResponseEntity<List<ServiceEntity>> listServices() {
        List<ServiceEntity> services = listServicesHandler.handle();
        return ResponseEntity.ok(services);
    }

    @Operation(summary = "List service types", description = "Lists available service types.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service types listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/service-types")
    public ResponseEntity<List<ServiceTypeEntity>> listServiceTypes() {
        List<ServiceTypeEntity> serviceType = listServiceTypesHandler.handle();
        return ResponseEntity.ok(serviceType);
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
                                                        content = @Content(
                                                                schema = @Schema(implementation = UpdateServiceRequest.class),
                                                                examples = @ExampleObject(value = "{\"serviceType\":\"ALINHAMENTO\",\"idOS\":\"b46ac51b-5ca6-439b-ba52-a36bd52e8648\"}")
                                                        )
                                                )
                                                @RequestBody UpdateServiceRequest request) {
        var service = updateServiceHandler.handle(id, request);
        return ResponseEntity.ok(service);
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
        var service = updateServiceStatusHandler.handle(request);
        return ResponseEntity.ok().body(service);
    }
}
