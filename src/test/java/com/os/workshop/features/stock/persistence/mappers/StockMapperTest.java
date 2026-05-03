package com.os.workshop.features.stock.persistence.mappers;

import com.os.workshop.features.stock.domain.Stock;
import com.os.workshop.features.stock.persistence.mappers.StockMapper;
import com.os.workshop.features.stock.persistence.mappers.StockMapperImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockMapperTest {

    private final StockMapper mapper = new StockMapperImpl();

    @Test
    void convertsAndRecalculatesAvailableQuantity() {
        Stock stock = stock();
        var entity = mapper.toEntity(stock);

        stock.setMinimumQuantity(new BigDecimal("2"));
        mapper.updateEntity(entity, stock);

        assertEquals(new BigDecimal("9"), mapper.toDomain(entity).getAvailableQuantity());
    }

    private Stock stock() {
        Stock stock = new Stock();
        stock.setProductId(2L);
        stock.setQuantity(BigDecimal.TEN);
        stock.setReservedQuantity(BigDecimal.ONE);
        stock.setMinimumQuantity(BigDecimal.ZERO);
        stock.recalculateAvailableQuantity();
        return stock;
    }
}
