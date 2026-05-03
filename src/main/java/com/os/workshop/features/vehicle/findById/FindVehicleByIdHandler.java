package com.os.workshop.features.vehicle.findById;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.exception.VehicleNotFoundException;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindVehicleByIdHandler {

    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Vehicle handle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("id: " + id));
    }
}
