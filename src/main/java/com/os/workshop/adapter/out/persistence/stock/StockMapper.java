package com.os.workshop.adapter.out.persistence.stock;

import com.os.workshop.domain.stock.Stock;
import com.os.workshop.infrastructure.persistence.stock.StockEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StockMapper {
    @Mapping(target = "availableQuantity", ignore = true)
    Stock toDomain(StockEntity entity);

    @AfterMapping
    default void afterToDomain(@MappingTarget Stock stock) { stock.recalculateAvailableQuantity(); }

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
