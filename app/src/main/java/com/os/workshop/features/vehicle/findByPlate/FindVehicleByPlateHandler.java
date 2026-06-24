package com.os.workshop.features.vehicle.findByPlate;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.exception.VehicleNotFoundException;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindVehicleByPlateHandler {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Vehicle handle(String plate) {
        String normalizedPlate = normalizePlate(plate);
        return vehicleRepository.findByPlate(normalizedPlate)
                .orElseThrow(() -> new VehicleNotFoundException("plate: " + plate));
    }

    private static String normalizePlate(String plate) {
        return plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
