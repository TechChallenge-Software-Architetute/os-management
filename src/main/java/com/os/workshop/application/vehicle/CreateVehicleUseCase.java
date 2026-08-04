package com.os.workshop.application.vehicle;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateVehicleUseCase {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public Vehicle execute(Long clientId, String plate, String brand,
                           String model, int year, String color, VehicleType type) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("id: " + clientId));

        String normalizedPlate = plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new IllegalStateException("A vehicle with plate '" + plate + "' already exists");
        }

        Vehicle vehicle = Vehicle.create(clientId, plate, brand, model, year, color, type);
        return vehicleRepository.save(vehicle);
    }
}
