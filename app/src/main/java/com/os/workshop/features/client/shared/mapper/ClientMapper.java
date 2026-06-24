package com.os.workshop.features.client.shared.mapper;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.repository.ClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper MapStruct responsável pela conversão entre o aggregate {@link Client} do domínio
 * e a entidade de persistência {@link ClientEntity}.
 */
@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "cpf", source = "cpf.value")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ClientEntity toEntity(Client client);

    @Mapping(target = "cpf", source = "cpf.value")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget ClientEntity entity, Client client);

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
