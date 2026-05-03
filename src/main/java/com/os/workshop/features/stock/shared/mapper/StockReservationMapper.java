package com.os.workshop.features.stock.shared.mapper;

import com.os.workshop.features.stock.shared.domain.StockReservation;
import com.os.workshop.features.stock.shared.repository.StockReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockReservationMapper {

    StockReservation toDomain(StockReservationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StockReservationEntity toEntity(StockReservation reservation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stockId", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "serviceOrderId", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget StockReservationEntity entity, StockReservation reservation);
}
