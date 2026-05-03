package com.os.workshop.features.service.shared.repository;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByIdOS(UUID idOS);
}
