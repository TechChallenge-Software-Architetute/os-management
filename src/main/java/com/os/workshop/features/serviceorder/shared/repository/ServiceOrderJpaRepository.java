package com.os.workshop.features.serviceorder.shared.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderEntity, UUID> {

    List<ServiceOrderEntity> findByCpfCnpj(String cpfCnpj);
}
