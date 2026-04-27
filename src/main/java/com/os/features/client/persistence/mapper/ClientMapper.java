package com.os.features.client.persistence.mapper;

import com.os.features.client.domain.Client;
import com.os.features.client.persistence.entity.ClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper MapStruct responsável pela conversão entre o aggregate {@link Client} do domínio
 * e a entidade de persistência {@link ClientEntity}.
 *
 * <p>Regras de mapeamento:
 * <ul>
 *   <li>{@code toEntity} — converte domínio → entidade para inserção (ignora ID e timestamps,
 *       que são gerenciados pelo JPA via {@code @GeneratedValue} e {@code @PrePersist}).</li>
 *   <li>{@code updateEntity} — atualiza os campos mutáveis de uma entidade já existente,
 *       preservando ID e {@code createdAt}.</li>
 *   <li>{@code toDomain} — reconstitui o aggregate a partir da entidade usando
 *       {@link Client#reconstitute}, que bypassa as validações de criação.</li>
 * </ul>
 *
 * <p>O campo {@code cpf} requer mapeamento explícito pois no domínio é um value object ({@link com.os.features.client.domain.valueobject.Cpf})
 * enquanto na entidade é uma {@code String}.
 */
@Mapper(componentModel = "spring")
public interface ClientMapper {

    /**
     * Converte um {@link Client} do domínio para uma nova {@link ClientEntity} (para insert).
     * ID e timestamps são ignorados — gerados pelo JPA.
     */
    @Mapping(target = "cpf", source = "cpf.value")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ClientEntity toEntity(Client client);

    /**
     * Aplica os campos mutáveis de um {@link Client} sobre uma {@link ClientEntity} existente (para update).
     * ID e timestamps são preservados da entidade original.
     */
    @Mapping(target = "cpf", source = "cpf.value")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget ClientEntity entity, Client client);

    /**
     * Reconstitui um {@link Client} do domínio a partir de uma {@link ClientEntity}.
     * Usa o factory method {@link Client#reconstitute} para não reaplicar validações de criação.
     */
    default Client toDomain(ClientEntity entity) {
        return Client.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getPhone(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
