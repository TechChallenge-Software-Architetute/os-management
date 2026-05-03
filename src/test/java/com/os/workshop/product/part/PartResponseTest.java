package com.os.workshop.product.part;

import com.os.workshop.features.product.domain.Part;
import com.os.workshop.features.product.domain.ProductType;
import com.os.workshop.features.product.domain.UnitOfMeasure;
import com.os.workshop.features.product.part.PartResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PartResponseTest {

    @Test
    void createsResponseFromPart() {
        Part part = new Part();
        part.setId(1L);
        part.setName("Pastilha");
        part.setSku("PST-1");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setCostPrice(new BigDecimal("10.00"));
        part.setSalePrice(new BigDecimal("18.00"));
        part.setActive(true);
        part.setManufacturerCode("M1");
        part.setWarrantyMonths(12);

        PartResponse response = PartResponse.from(part);

        assertEquals("PST-1", response.sku());
        assertEquals("M1", response.manufacturerCode());
        assertEquals(12, response.warrantyMonths());
    }
}
