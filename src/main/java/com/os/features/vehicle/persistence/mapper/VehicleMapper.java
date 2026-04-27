package com.os.features.vehicle.persistence.mapper;

import com.os.features.client.persistence.entity.ClientEntity;
import com.os.features.vehicle.domain.Vehicle;
import com.os.features.vehicle.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper MapStruct responsável pela conversão entre o aggregate {@link Vehicle} do domínio
 * e a entidade de persistência {@link VehicleEntity}.
 *
 * <p>Desafios de mapeamento tratados aqui:
 * <ul>
 *   <li>{@code plate}: no domínio é {@code LicensePlate} (value object); na entidade é {@code String}.
 *       Mapeado com {@code source = "vehicle.plate.value"} para extrair o valor normalizado.</li>
 *   <li>{@code client}: o domínio referencia o cliente por {@code clientId} (Long);
 *       a entidade precisa do {@link ClientEntity} completo para o {@code @ManyToOne}.
 *       Por isso {@code toEntity} recebe o {@link ClientEntity} como segundo parâmetro,
 *       resolvido pelo adapter antes de chamar o mapper.</li>
 *   <li>Ambiguidade em {@code toEntity}: como há dois parâmetros de origem ({@code vehicle} e
 *       {@code client}), todos os campos são mapeados explicitamente para evitar conflitos
 *       em campos com o mesmo nome (ex: {@code active}).</li>
 * </ul>
 *
 * <p>O método {@code toDomain} é implementado manualmente ({@code default}) porque o domínio
 * usa o factory method {@link Vehicle#reconstitute} para reconstrução, não setters convencionais.
 */
@Mapper(componentModel = "spring")
public interface VehicleMapper {

    /**
     * Converte um {@link Vehicle} + {@link ClientEntity} para uma nova {@link VehicleEntity} (para insert).
     * O {@link ClientEntity} é passado separadamente pois o domínio mantém apenas o {@code clientId}.
     * ID e timestamps são ignorados — gerados pelo JPA.
     */
    @Mapping(target = "plate",  source = "vehicle.plate.value")
    @Mapping(target = "client", source = "client")
    @Mapping(target = "brand",  source = "vehicle.brand")
    @Mapping(target = "model",  source = "vehicle.model")
    @Mapping(target = "year",   source = "vehicle.year")
    @Mapping(target = "color",  source = "vehicle.color")
    @Mapping(target = "type",   source = "vehicle.type")
    @Mapping(target = "active", source = "vehicle.active")
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    VehicleEntity toEntity(Vehicle vehicle, ClientEntity client);

    /**
     * Aplica os campos mutáveis de um {@link Vehicle} sobre uma {@link VehicleEntity} existente (para update).
     * O relacionamento com o cliente ({@code client}) não é alterado pelo mapper —
     * o adapter atualiza-o explicitamente caso necessário.
     */
    @Mapping(target = "plate",      source = "plate.value")
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "client",     ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateEntity(@MappingTarget VehicleEntity entity, Vehicle vehicle);

    /**
     * Reconstitui um {@link Vehicle} do domínio a partir de uma {@link VehicleEntity}.
     * Usa o factory method {@link Vehicle#reconstitute} para não reaplicar validações de criação.
     * Acessa {@code entity.client.id} para obter o {@code clientId} sem carregar o aggregate completo.
     */
    default Vehicle toDomain(VehicleEntity entity) {
        return Vehicle.reconstitute(
                entity.getId(),
                entity.getClient().getId(),
                entity.getPlate(),
                entity.getBrand(),
                entity.getModel(),
                entity.getYear(),
                entity.getColor(),
                entity.getType(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
