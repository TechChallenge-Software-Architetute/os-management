package com.os.workshop.stock.persistence.mappers;

import com.os.workshop.stock.domain.Stock;
import com.os.workshop.stock.persistence.entity.StockEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "availableQuantity", ignore = true)
    Stock toDomain(StockEntity entity);

    @AfterMapping
    default void afterToDomain(@MappingTarget Stock stock) {
        stock.recalculateAvailableQuantity();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StockEntity toEntity(Stock stock);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget StockEntity entity, Stock stock);
}
