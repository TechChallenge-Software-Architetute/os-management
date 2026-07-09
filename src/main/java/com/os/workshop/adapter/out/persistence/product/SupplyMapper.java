package com.os.workshop.adapter.out.persistence.product;

import com.os.workshop.domain.product.Supply;
import com.os.workshop.infrastructure.persistence.product.SupplyEntity;
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
