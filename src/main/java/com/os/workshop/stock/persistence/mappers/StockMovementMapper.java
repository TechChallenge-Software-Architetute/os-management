package com.os.workshop.stock.persistence.mappers;

import com.os.workshop.stock.domain.StockMovement;
import com.os.workshop.stock.persistence.entity.StockMovementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    StockMovement toDomain(StockMovementEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    StockMovementEntity toEntity(StockMovement movement);
}
