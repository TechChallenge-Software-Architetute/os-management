package com.os.workshop.application.vehicle;

import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import com.os.workshop.domain.vehicle.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateVehicleUseCase {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle execute(Long id, String plate, String brand,
                           String model, int year, String color, VehicleType type) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("id: " + id));

        String normalizedPlate = plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (!vehicle.getPlate().getValue().equals(normalizedPlate)
                && vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new IllegalStateException("A vehicle with plate '" + plate + "' already exists");
        }

        vehicle.update(plate, brand, model, year, color, type);
        return vehicleRepository.save(vehicle);
    }
}
