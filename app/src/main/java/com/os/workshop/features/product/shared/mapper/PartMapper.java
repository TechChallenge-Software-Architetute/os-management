package com.os.workshop.features.product.shared.mapper;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.repository.PartEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PartMapper {

    Part toDomain(PartEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PartEntity toEntity(Part part);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget PartEntity entity, Part part);
}
