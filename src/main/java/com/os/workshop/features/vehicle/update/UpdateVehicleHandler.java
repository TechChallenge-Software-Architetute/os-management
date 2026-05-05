package com.os.workshop.features.vehicle.update;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.exception.VehicleNotFoundException;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateVehicleHandler {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle handle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("id: " + id));

        String normalizedPlate = normalizePlate(request.plate());
        if (!vehicle.getPlate().getValue().equals(normalizedPlate)
                && vehicleRepository.existsByPlate(normalizedPlate)) {
            throw new IllegalStateException("A vehicle with plate '" + request.plate() + "' already exists");
        }

        vehicle.update(request.plate(), request.brand(), request.model(),
                request.year(), request.color(), request.type());
        return vehicleRepository.save(vehicle);
    }

    private static String normalizePlate(String plate) {
        return plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
