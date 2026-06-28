package com.os.workshop.features.vehicle.create;

import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateVehicleHandler {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public Vehicle handle(CreateVehicleRequest request) {
        clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ClientNotFoundException("id: " + request.clientId()));

        String normalizedPlate = normalizePlate(request.plate());
        if (vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new IllegalStateException("A vehicle with plate '" + request.plate() + "' already exists");
        }

        Vehicle vehicle = Vehicle.create(
                request.clientId(), request.plate(),
                request.brand(), request.model(),
                request.year(), request.color(), request.type()
        );
        return vehicleRepository.save(vehicle);
    }

    private static String normalizePlate(String plate) {
        return plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
