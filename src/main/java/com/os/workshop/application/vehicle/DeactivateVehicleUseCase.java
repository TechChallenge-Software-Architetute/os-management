package com.os.workshop.application.vehicle;

import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateVehicleUseCase {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public void execute(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("id: " + id));
        vehicle.deactivate();
        vehicleRepository.save(vehicle);
    }
}
