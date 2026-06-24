package com.os.workshop.features.vehicle;

import com.os.workshop.features.vehicle.create.CreateVehicleHandler;
import com.os.workshop.features.vehicle.create.CreateVehicleRequest;
import com.os.workshop.features.vehicle.create.CreateVehicleResponse;
import com.os.workshop.features.vehicle.deactivate.DeactivateVehicleHandler;
import com.os.workshop.features.vehicle.findByClient.FindVehiclesByClientHandler;
import com.os.workshop.features.vehicle.findByClient.FindVehiclesByClientResponse;
import com.os.workshop.features.vehicle.findById.FindVehicleByIdHandler;
import com.os.workshop.features.vehicle.findById.FindVehicleByIdResponse;
import com.os.workshop.features.vehicle.findByPlate.FindVehicleByPlateHandler;
import com.os.workshop.features.vehicle.findByPlate.FindVehicleByPlateResponse;
import com.os.workshop.features.vehicle.update.UpdateVehicleHandler;
import com.os.workshop.features.vehicle.update.UpdateVehicleRequest;
import com.os.workshop.features.vehicle.update.UpdateVehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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

/**
 * Controller REST para operações CRUD de veículos.
 *
 * <p>Endpoints disponíveis:
 * <ul>
 *   <li>{@code POST   /api/vehicles}                        — cadastra um novo veículo</li>
 *   <li>{@code GET    /api/vehicles/{id}}                   — busca veículo por ID</li>
 *   <li>{@code GET    /api/vehicles/plate/{plate}}           — busca veículo por placa</li>
 *   <li>{@code GET    /api/vehicles/client/{clientId}}       — lista todos os veículos de um cliente</li>
 *   <li>{@code PUT    /api/vehicles/{id}}                   — atualiza dados do veículo</li>
 *   <li>{@code DELETE /api/vehicles/{id}}                   — desativa o veículo (soft delete)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Manage client vehicles.")
public class VehicleController {

    private final CreateVehicleHandler createVehicleHandler;
    private final FindVehicleByIdHandler findVehicleByIdHandler;
    private final FindVehicleByPlateHandler findVehicleByPlateHandler;
    private final FindVehiclesByClientHandler findVehiclesByClientHandler;
    private final UpdateVehicleHandler updateVehicleHandler;
    private final DeactivateVehicleHandler deactivateVehicleHandler;

    @Operation(summary = "Create vehicle", description = "Creates a vehicle for an existing client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<CreateVehicleResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Vehicle data.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateVehicleRequest.class))
            )
            @Valid @RequestBody CreateVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateVehicleResponse.from(createVehicleHandler.handle(request)));
    }

    @Operation(summary = "Find vehicle by ID", description = "Returns a vehicle by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FindVehicleByIdResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FindVehicleByIdResponse.from(findVehicleByIdHandler.handle(id)));
    }

    @Operation(summary = "Find vehicle by plate", description = "Returns a vehicle by its plate.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/plate/{plate}")
    public ResponseEntity<FindVehicleByPlateResponse> findByPlate(@PathVariable String plate) {
        return ResponseEntity.ok(FindVehicleByPlateResponse.from(findVehicleByPlateHandler.handle(plate)));
    }

    @Operation(summary = "List client vehicles", description = "Lists all vehicles linked to a client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles listed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FindVehiclesByClientResponse>> findAllByClient(@PathVariable Long clientId) {
        List<FindVehiclesByClientResponse> response = findVehiclesByClientHandler.handle(clientId).stream()
                .map(FindVehiclesByClientResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update vehicle", description = "Updates vehicle data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UpdateVehicleResponse> update(@PathVariable Long id,
                                                         @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                                 description = "Updated vehicle data.",
                                                                 required = true,
                                                                 content = @Content(schema = @Schema(implementation = UpdateVehicleRequest.class))
                                                         )
                                                         @Valid @RequestBody UpdateVehicleRequest request) {
        return ResponseEntity.ok(UpdateVehicleResponse.from(updateVehicleHandler.handle(id, request)));
    }

    @Operation(summary = "Deactivate vehicle", description = "Deactivates a vehicle by its identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateVehicleHandler.handle(id);
        return ResponseEntity.noContent().build();
    }
}
