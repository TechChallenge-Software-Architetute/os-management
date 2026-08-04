package com.os.workshop.application.vehicle;

import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindVehicleByPlateUseCase {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Vehicle execute(String plate) {
        String normalizedPlate = plate == null ? "" : plate.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return vehicleRepository.findByPlate(normalizedPlate)
                .orElseThrow(() -> new VehicleNotFoundException("plate: " + plate));
    }
}
