package com.os.workshop.infrastructure.persistence.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientJpaRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByDocument(String document);

    List<ClientEntity> findByActiveTrue();

    boolean existsByDocument(String document);

    Optional<ClientEntity> findByEmail(String email);
}
