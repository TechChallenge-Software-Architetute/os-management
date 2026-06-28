package com.os.workshop.features.vehicle.findByClient;

import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindVehiclesByClientHandler {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<Vehicle> handle(Long clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("id: " + clientId));
        return vehicleRepository.findAllByClientId(clientId);
    }
}
