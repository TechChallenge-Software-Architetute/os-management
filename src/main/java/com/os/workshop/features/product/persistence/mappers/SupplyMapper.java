package com.os.workshop.features.product.persistence.mappers;

import com.os.workshop.features.product.domain.Supply;
import com.os.workshop.features.product.persistence.entity.SupplyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplyMapper {

    Supply toDomain(SupplyEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SupplyEntity toEntity(Supply supply);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget SupplyEntity entity, Supply supply);
}
