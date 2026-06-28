package com.os.workshop.infrastructure.persistence.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientJpaRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByCpf(String cpf);

    List<ClientEntity> findByActiveTrue();

    boolean existsByCpf(String cpf);

    Optional<ClientEntity> findByEmail(String email);
}
