package com.os.workshop.stock.management;

import com.os.workshop.features.stock.domain.Stock;
import com.os.workshop.features.stock.management.StockResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockResponseTest {

    @Test
    void exposesCalculatedAvailableQuantity() {
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setProductId(2L);
        stock.setQuantity(new BigDecimal("10"));
        stock.setReservedQuantity(new BigDecimal("3"));
        stock.setMinimumQuantity(new BigDecimal("5"));
        stock.recalculateAvailableQuantity();

        assertEquals(new BigDecimal("7"), StockResponse.from(stock).availableQuantity());
    }
}
