package com.os.features.vehicle.repository;

import com.os.features.vehicle.domain.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída (output port) para operações de persistência de {@link Vehicle}.
 *
 * <p>Interface de domínio implementada pelo adaptador de infraestrutura {@code VehiclePersistenceAdapter}.
 */
public interface VehicleRepository {

    /**
     * Persiste um veículo (insert ou update conforme presença de ID).
     *
     * @param vehicle veículo a persistir
     * @return veículo com ID e timestamps preenchidos
     */
    Vehicle save(Vehicle vehicle);

    /**
     * Busca um veículo pelo seu identificador único.
     *
     * @param id ID do veículo
     * @return {@code Optional} com o veículo ou vazio se não existir
     */
    Optional<Vehicle> findById(Long id);

    /**
     * Busca um veículo pela placa normalizada (sem separadores, maiúscula).
     *
     * @param normalizedPlate placa sem hífen, em maiúsculas (ex: {@code "ABC1234"})
     * @return {@code Optional} com o veículo ou vazio se não existir
     */
    Optional<Vehicle> findByPlate(String normalizedPlate);

    /**
     * Retorna todos os veículos ativos de um determinado cliente.
     *
     * @param clientId ID do cliente proprietário
     * @return lista de veículos ativos do cliente
     */
    List<Vehicle> findAllByClientId(Long clientId);

    /**
     * Verifica se já existe um veículo com a placa informada.
     *
     * @param normalizedPlate placa sem hífen, em maiúsculas
     * @return {@code true} se a placa já estiver cadastrada
     */
    boolean existsByPlate(String normalizedPlate);
}
