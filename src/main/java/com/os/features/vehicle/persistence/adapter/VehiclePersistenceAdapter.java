package com.os.features.vehicle.persistence.adapter;

import com.os.features.client.persistence.repository.ClientJpaRepository;
import com.os.features.vehicle.domain.Vehicle;
import com.os.features.vehicle.persistence.entity.VehicleEntity;
import com.os.features.vehicle.persistence.mapper.VehicleMapper;
import com.os.features.vehicle.persistence.repository.VehicleJpaRepository;
import com.os.features.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa a porta {@link VehicleRepository} usando Spring Data JPA.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Resolver o {@code ClientEntity} a partir do {@code clientId} do aggregate —
 *       necessário para popular o relacionamento {@code @ManyToOne} da entidade JPA.</li>
 *   <li>Decidir entre insert e update com base na presença de ID no aggregate.</li>
 *   <li>Converter entre o domínio ({@link Vehicle}) e a entidade JPA ({@link VehicleEntity})
 *       através do {@link VehicleMapper}.</li>
 * </ul>
 *
 * <p>O {@link ClientJpaRepository} é injetado diretamente aqui (infraestrutura → infraestrutura)
 * pois este adapter precisa do {@link com.os.features.client.persistence.entity.ClientEntity}
 * para montar o relacionamento JPA, algo que não faz sentido expor via porta de domínio.
 */
@Component
@RequiredArgsConstructor
public class VehiclePersistenceAdapter implements VehicleRepository {

    private final VehicleJpaRepository vehicleJpaRepository;
    private final ClientJpaRepository clientJpaRepository;
    private final VehicleMapper vehicleMapper;

    /**
     * Persiste o veículo. Resolve o {@code ClientEntity} pelo {@code clientId} antes de mapear,
     * pois o mapper precisa do objeto completo para montar o {@code @ManyToOne}.
     * Se o aggregate já possui ID, carrega a entidade existente e aplica o update;
     * caso contrário, cria uma nova entidade para insert.
     */
    @Override
    public Vehicle save(Vehicle vehicle) {
        var clientEntity = clientJpaRepository.findById(vehicle.getClientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client not found in persistence: " + vehicle.getClientId()));

        VehicleEntity entity;
        if (vehicle.getId() != null) {
            entity = vehicleJpaRepository.findById(vehicle.getId())
                    .orElse(vehicleMapper.toEntity(vehicle, clientEntity));
            vehicleMapper.updateEntity(entity, vehicle);
            entity.setClient(clientEntity);
        } else {
            entity = vehicleMapper.toEntity(vehicle, clientEntity);
        }

        return vehicleMapper.toDomain(vehicleJpaRepository.save(entity));
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return vehicleJpaRepository.findById(id).map(vehicleMapper::toDomain);
    }

    @Override
    public Optional<Vehicle> findByPlate(String normalizedPlate) {
        return vehicleJpaRepository.findByPlate(normalizedPlate).map(vehicleMapper::toDomain);
    }

    @Override
    public List<Vehicle> findAllByClientId(Long clientId) {
        return vehicleJpaRepository.findByClient_IdAndActiveTrue(clientId).stream()
                .map(vehicleMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByPlate(String normalizedPlate) {
        return vehicleJpaRepository.existsByPlate(normalizedPlate);
    }
}
