package com.os.workshop.features.vehicle;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.vehicle.dto.VehicleRequest;
import com.os.workshop.features.vehicle.dto.VehicleResponse;
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
@Tag(name = "Vehicles", description = "Manage client vehicles.")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Cadastra um novo veículo para um cliente existente.
     * A placa deve ser única no sistema e o cliente deve existir.
     *
     * @param request dados do veículo
     * @return veículo criado com HTTP 201
     */
    @Operation(summary = "Create resource", description = "Create resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<VehicleResponse> create(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Request payload for this operation",
        required = true,
        content = @Content(schema = @Schema(implementation = VehicleRequest.class))
)
@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VehicleResponse.from(vehicleService.create(request)));
    }

    /**
     * Busca um veículo pelo seu identificador único.
     *
     * @param id ID do veículo
     * @return dados do veículo com HTTP 200, ou 404 se não encontrado
     */
    @Operation(summary = "Find resource by id", description = "Find resource by id endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.findById(id)));
    }

    /**
     * Busca um veículo pela placa.
     * Aceita placa com ou sem hífen e em qualquer capitalização.
     *
     * @param plate placa do veículo (ex: {@code ABC1234} ou {@code ABC-1234})
     * @return dados do veículo com HTTP 200, ou 404 se não encontrado
     */
    @Operation(summary = "Find by plate", description = "Find by plate endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleResponse> findByPlate(@PathVariable String plate) {
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.findByPlate(plate)));
    }

    /**
     * Lista todos os veículos ativos de um cliente.
     *
     * @param clientId ID do cliente proprietário
     * @return lista de veículos com HTTP 200, ou 404 se o cliente não existir
     */
    @Operation(summary = "Find all by client", description = "Find all by client endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<VehicleResponse>> findAllByClient(@PathVariable Long clientId) {
        List<VehicleResponse> response = vehicleService.findAllByClient(clientId).stream()
                .map(VehicleResponse::from)
                .toList();
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
    @Operation(summary = "Update resource", description = "Update resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id,
                                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(

                                                           description = "Request payload for this operation",

                                                           required = true,

                                                           content = @Content(schema = @Schema(implementation = VehicleRequest.class))

                                                   )

                                                   @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.update(id, request)));
    }

    /**
     * Desativa um veículo (soft delete).
     * O veículo permanece no banco mas não aparece mais nas listagens ativas.
     *
     * @param id ID do veículo
     * @return HTTP 204 em caso de sucesso, ou 404 se não encontrado
     */
    @Operation(summary = "Deactivate resource", description = "Deactivate resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        vehicleService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
