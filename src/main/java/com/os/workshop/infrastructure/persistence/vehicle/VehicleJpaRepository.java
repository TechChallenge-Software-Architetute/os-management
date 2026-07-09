package com.os.workshop.infrastructure.persistence.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, Long> {
    Optional<VehicleEntity> findByPlate(String plate);
    List<VehicleEntity> findByClient_IdAndActiveTrue(Long clientId);
    boolean existsByPlate(String plate);
}
