package com.os.workshop.service.repository;

import com.os.workshop.service.domain.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    // Métodos customizados podem ser adicionados aqui, se necessário
}