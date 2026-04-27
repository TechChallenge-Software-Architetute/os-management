package com.os.features.vehicle;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

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
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Cadastra um novo veículo para um cliente existente.
     * A placa deve ser única no sistema e o cliente deve existir.
     *
     * @param request dados do veículo
     * @return veículo criado com HTTP 201
     */
    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VehicleResponse.from(vehicleService.create(request)));
    }

    /**
     * Busca um veículo pelo seu identificador único.
     *
     * @param id UUID do veículo
     * @return dados do veículo com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.findById(id)));
    }

    /**
     * Busca um veículo pela placa.
     * Aceita placa com ou sem hífen e em qualquer capitalização.
     *
     * @param plate placa do veículo (ex: {@code ABC1234} ou {@code ABC-1234})
     * @return dados do veículo com HTTP 200, ou 404 se não encontrado
     */
    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleResponse> findByPlate(@PathVariable String plate) {
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.findByPlate(plate)));
    }

    /**
     * Lista todos os veículos ativos de um cliente.
     *
     * @param clientId UUID do cliente proprietário
     * @return lista de veículos com HTTP 200, ou 404 se o cliente não existir
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<VehicleResponse>> findAllByClient(@PathVariable UUID clientId) {
        List<VehicleResponse> response = vehicleService.findAllByClient(clientId).stream()
                .map(VehicleResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza os dados de um veículo existente.
     * O cliente proprietário não pode ser alterado.
     *
     * @param id      UUID do veículo
     * @param request novos dados do veículo
     * @return veículo atualizado com HTTP 200, ou 404 se não encontrado
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(VehicleResponse.from(vehicleService.update(id, request)));
    }

    /**
     * Desativa um veículo (soft delete).
     * O veículo permanece no banco mas não aparece mais nas listagens ativas.
     *
     * @param id UUID do veículo
     * @return HTTP 204 em caso de sucesso, ou 404 se não encontrado
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        vehicleService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
