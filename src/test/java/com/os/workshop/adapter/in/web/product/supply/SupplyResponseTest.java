package com.os.workshop.adapter.in.web.product.supply;

import com.os.workshop.domain.product.ProductType;
import com.os.workshop.domain.product.Supply;
import com.os.workshop.domain.product.UnitOfMeasure;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplyResponseTest {

    @Test
    void createsResponseFromSupply() {
        Supply supply = new Supply();
        supply.setId(2L);
        supply.setName("Óleo");
        supply.setSku("OIL-1");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setCostPrice(new BigDecimal("30.00"));
        supply.setSalePrice(new BigDecimal("45.00"));
        supply.setActive(true);
        supply.setFractionalAllowed(true);
        supply.setPackageSize(new BigDecimal("1.00"));

        SupplyResponse response = SupplyResponse.from(supply);

        assertEquals("OIL-1", response.sku());
        assertTrue(response.fractionalAllowed());
    }
}
