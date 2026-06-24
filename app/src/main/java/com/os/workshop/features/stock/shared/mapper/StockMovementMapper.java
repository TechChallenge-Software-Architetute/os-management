package com.os.workshop.features.stock.shared.mapper;

import com.os.workshop.features.stock.shared.domain.StockMovement;
import com.os.workshop.features.stock.shared.repository.StockMovementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    StockMovement toDomain(StockMovementEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    StockMovementEntity toEntity(StockMovement movement);
}
