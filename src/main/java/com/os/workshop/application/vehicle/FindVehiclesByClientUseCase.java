package com.os.workshop.application.vehicle;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.vehicle.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindVehiclesByClientUseCase {

    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<Vehicle> execute(Long clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("id: " + clientId));
        return vehicleRepository.findAllByClientId(clientId);
    }
}
