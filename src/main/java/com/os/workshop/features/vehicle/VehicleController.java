package com.os.workshop.features.vehicle;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.vehicle.dto.VehicleRequest;
import com.os.workshop.features.vehicle.dto.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Tag(name = "Vehicles", description = "Manage customer vehicles and ownership relationships.")
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    private final VehicleService vehicleService;

    /**
     * Cadastra um novo veículo para um cliente existente.
     * A placa deve ser única no sistema e o cliente deve existir.
     *
     * @param request dados do veículo
     * @return veículo criado com HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create vehicle", description = "Creates a new active vehicle for an existing client. The license plate must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully", content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicated license plate", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Vehicle data to create.", required = true, content = @Content(schema = @Schema(implementation = VehicleRequest.class)))
            @Valid @RequestBody VehicleRequest request) {
        logger.info("Creating vehicle. clientId={}, plate={}", request.clientId(), request.plate());
        var response = VehicleResponse.from(vehicleService.create(request));
        logger.info("Vehicle created. id={}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um veículo pelo seu identificador único.
     *
     * @param id ID do veículo
     * @return dados do veículo com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Find vehicle by ID", description = "Retrieves an active vehicle by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle found", content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> findById(
            @Parameter(description = "Vehicle unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Finding vehicle by id. id={}", id);
        var response = VehicleResponse.from(vehicleService.findById(id));
        logger.info("Vehicle found. id={}", response.id());
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um veículo pela placa.
     * Aceita placa com ou sem hífen e em qualquer capitalização.
     *
     * @param plate placa do veículo (ex: {@code ABC1234} ou {@code ABC-1234})
     * @return dados do veículo com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/plate/{plate}")
    @Operation(summary = "Find vehicle by plate", description = "Retrieves an active vehicle by license plate. Plates can be sent with or without hyphen.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle found", content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid license plate", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> findByPlate(
            @Parameter(description = "Vehicle license plate.", example = "ABC-1234", required = true)
            @PathVariable String plate) {
        logger.info("Finding vehicle by plate. plate={}", plate);
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.findByPlate(plate)));
    }

    /**
     * Lista todos os veículos ativos de um cliente.
     *
     * @param clientId ID do cliente proprietário
     * @return lista de veículos com HTTP 200, ou 404 se o cliente não existir
     */
    @GetMapping("/client/{clientId}")
    @Operation(summary = "List vehicles by client", description = "Lists all active vehicles owned by a client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicles listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = VehicleResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid client identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<VehicleResponse>> findAllByClient(
            @Parameter(description = "Owner client unique identifier.", example = "1", required = true)
            @PathVariable Long clientId) {
        logger.info("Listing vehicles by client. clientId={}", clientId);
        List<VehicleResponse> response = vehicleService.findAllByClient(clientId).stream()
                .map(VehicleResponse::from)
                .toList();
        logger.info("Vehicles listed by client. clientId={}, count={}", clientId, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza os dados de um veículo existente.
     * O cliente proprietário não pode ser alterado.
     *
     * @param id      ID do veículo
     * @param request novos dados do veículo
     * @return veículo atualizado com HTTP 200, ou 404 se não encontrado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Updates an existing vehicle. The owner client cannot be changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully", content = @Content(schema = @Schema(implementation = VehicleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle or client not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponse> update(
            @Parameter(description = "Vehicle unique identifier.", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Vehicle data to update.", required = true, content = @Content(schema = @Schema(implementation = VehicleRequest.class)))
            @Valid @RequestBody VehicleRequest request) {
        logger.info("Updating vehicle. id={}", id);
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.update(id, request)));
    }

    /**
     * Desativa um veículo (soft delete).
     * O veículo permanece no banco mas não aparece mais nas listagens ativas.
     *
     * @param id ID do veículo
     * @return HTTP 204 em caso de sucesso, ou 404 se não encontrado
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate vehicle", description = "Soft-deletes a vehicle so it no longer appears in active listings.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehicle deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "Vehicle unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Deactivating vehicle. id={}", id);
        vehicleService.deactivate(id);
        logger.info("Vehicle deactivated. id={}", id);
        return ResponseEntity.noContent().build();
    }
}
