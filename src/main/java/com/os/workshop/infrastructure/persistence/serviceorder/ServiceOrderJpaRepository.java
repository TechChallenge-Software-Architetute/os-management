package com.os.workshop.infrastructure.persistence.serviceorder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderEntity, UUID> {
    List<ServiceOrderEntity> findByCpfCnpj(String cpfCnpj);
}
