package com.os.workshop.features.product.supply;

import com.os.workshop.features.product.shared.domain.ProductType;
import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import com.os.workshop.features.product.supply.create.CreateSupplyResponse;
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

        CreateSupplyResponse response = CreateSupplyResponse.from(supply);

        assertEquals("OIL-1", response.sku());
        assertTrue(response.fractionalAllowed());
    }
}
