package com.os.workshop.infrastructure.persistence.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByIdOS(UUID idOS);
}
