package com.os.workshop.infrastructure.persistence.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceTypeJpaRepository extends JpaRepository<ServiceTypeEntity, UUID> {
    Optional<ServiceTypeEntity> findByName(String name);
}
