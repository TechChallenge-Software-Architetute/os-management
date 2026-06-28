package com.os.workshop.adapter.in.web.vehicle;

import com.os.workshop.application.vehicle.*;
import com.os.workshop.domain.vehicle.Vehicle;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final FindVehicleByIdUseCase findVehicleByIdUseCase;
    private final FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    private final FindVehiclesByClientUseCase findVehiclesByClientUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeactivateVehicleUseCase deactivateVehicleUseCase;

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = createVehicleUseCase.execute(request.clientId(), request.plate(),
                request.brand(), request.model(), request.year(), request.color(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponse.from(vehicle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(VehicleResponse.from(findVehicleByIdUseCase.execute(id)));
    }

    @GetMapping("/plate/{plate}")
    public ResponseEntity<VehicleResponse> findByPlate(@PathVariable String plate) {
        return ResponseEntity.ok(VehicleResponse.from(findVehicleByPlateUseCase.execute(plate)));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<VehicleResponse>> findAllByClient(@PathVariable Long clientId) {
        var response = findVehiclesByClientUseCase.execute(clientId).stream()
                .map(VehicleResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateVehicleRequest request) {
        Vehicle vehicle = updateVehicleUseCase.execute(id, request.plate(), request.brand(),
                request.model(), request.year(), request.color(), request.type());
        return ResponseEntity.ok(VehicleResponse.from(vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateVehicleUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
