package com.os.workshop.features.vehicle.shared.repository;

import com.os.workshop.features.vehicle.shared.domain.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(Long id);
    Optional<Vehicle> findByPlate(String normalizedPlate);
    List<Vehicle> findAllByClientId(Long clientId);
    boolean existsByPlate(String normalizedPlate);
}
