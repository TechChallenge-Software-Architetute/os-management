package com.os.workshop.features.service.adapter.database;

import com.os.workshop.features.service.domain.ServiceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, UUID> {
    Optional<ServiceTypeEntity> findByName(String name);
}
