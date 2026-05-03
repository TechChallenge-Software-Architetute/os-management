package com.os.workshop.features.vehicle.persistence.repository;

import com.os.workshop.features.vehicle.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para {@link VehicleEntity}.
 *
 * <p>Usado exclusivamente pelo {@code VehiclePersistenceAdapter} — não deve ser injetado
 * diretamente em serviços de aplicação ou domínio, mantendo o isolamento de camadas.
 *
 * <p>A query derivada {@code findByClient_IdAndActiveTrue} usa underscore para explicitar
 * a navegação pela associação {@code client → id}, evitando ambiguidade com o Spring Data.
 */
@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, Long> {

    /**
     * Busca um veículo pela placa normalizada (sem hífen, maiúscula).
     *
     * @param plate placa sem formatação (ex: {@code "ABC1234"})
     * @return {@code Optional} com a entidade ou vazio
     */
    Optional<VehicleEntity> findByPlate(String plate);

    /**
     * Retorna todos os veículos ativos de um determinado cliente.
     * O underscore em {@code Client_Id} instrui o Spring Data a navegar pela associação
     * {@code client} até a propriedade {@code id} da {@code ClientEntity}.
     *
     * @param clientId ID do cliente proprietário
     * @return lista de entidades com {@code active = true}
     */
    List<VehicleEntity> findByClient_IdAndActiveTrue(Long clientId);

    /**
     * Verifica se já existe um veículo com a placa informada.
     *
     * @param plate placa sem formatação (ex: {@code "ABC1234"})
     * @return {@code true} se a placa já estiver cadastrada
     */
    boolean existsByPlate(String plate);
}
