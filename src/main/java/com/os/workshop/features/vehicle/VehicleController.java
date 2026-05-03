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
public class VehicleController {

    private final CreateVehicleHandler createVehicleHandler;
    private final FindVehicleByIdHandler findVehicleByIdHandler;
    private final FindVehicleByPlateHandler findVehicleByPlateHandler;
    private final FindVehiclesByClientHandler findVehiclesByClientHandler;
    private final UpdateVehicleHandler updateVehicleHandler;
    private final DeactivateVehicleHandler deactivateVehicleHandler;

    @PostMapping
    public ResponseEntity<CreateVehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateVehicleResponse.from(createVehicleHandler.handle(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindVehicleByIdResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FindVehicleByIdResponse.from(findVehicleByIdHandler.handle(id)));
    }

    @GetMapping("/plate/{plate}")
    public ResponseEntity<FindVehicleByPlateResponse> findByPlate(@PathVariable String plate) {
        return ResponseEntity.ok(FindVehicleByPlateResponse.from(findVehicleByPlateHandler.handle(plate)));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<FindVehiclesByClientResponse>> findAllByClient(@PathVariable Long clientId) {
        List<FindVehiclesByClientResponse> response = findVehiclesByClientHandler.handle(clientId).stream()
                .map(FindVehiclesByClientResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateVehicleResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateVehicleRequest request) {
        return ResponseEntity.ok(UpdateVehicleResponse.from(updateVehicleHandler.handle(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateVehicleHandler.handle(id);
        return ResponseEntity.noContent().build();
    }
}
