package com.os.workshop.adapter.out.persistence.vehicle;

import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.infrastructure.persistence.client.ClientJpaRepository;
import com.os.workshop.infrastructure.persistence.vehicle.VehicleEntity;
import com.os.workshop.infrastructure.persistence.vehicle.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VehiclePersistenceAdapter implements VehicleRepository {

    private final VehicleJpaRepository vehicleJpaRepository;
    private final ClientJpaRepository clientJpaRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    public Vehicle save(Vehicle vehicle) {
        var clientEntity = clientJpaRepository.findById(vehicle.getClientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client not found in persistence: " + vehicle.getClientId()));

        VehicleEntity entity;
        if (vehicle.getId() != null) {
            entity = vehicleJpaRepository.findById(vehicle.getId())
                    .orElse(vehicleMapper.toEntity(vehicle, clientEntity));
            vehicleMapper.updateEntity(entity, vehicle);
            entity.setClient(clientEntity);
        } else {
            entity = vehicleMapper.toEntity(vehicle, clientEntity);
        }

        return vehicleMapper.toDomain(vehicleJpaRepository.save(entity));
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return vehicleJpaRepository.findById(id).map(vehicleMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByPlate(String normalizedPlate) {
        return vehicleJpaRepository.findByPlate(normalizedPlate).map(vehicleMapper::toDomain);
    }

    @Override
    public List<Vehicle> findAllByClientId(Long clientId) {
        return vehicleJpaRepository.findByClient_IdAndActiveTrue(clientId).stream()
                .map(vehicleMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByPlate(String normalizedPlate) {
        return vehicleJpaRepository.existsByPlate(normalizedPlate);
    }
}
