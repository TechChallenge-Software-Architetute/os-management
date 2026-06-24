package com.os.workshop.features.vehicle.deactivate;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.exception.VehicleNotFoundException;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateVehicleHandler {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public void handle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("id: " + id));
        vehicle.deactivate();
        vehicleRepository.save(vehicle);
    }
}
