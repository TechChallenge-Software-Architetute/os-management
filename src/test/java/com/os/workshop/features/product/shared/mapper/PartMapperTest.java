package com.os.workshop.features.product.shared.mapper;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.domain.ProductType;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PartMapperTest {

    private final PartMapper mapper = new PartMapperImpl();

    @Test
    void convertsAndUpdatesEntity() {
        Part part = part();
        var entity = mapper.toEntity(part);

        part.setName("Updated Part");
        mapper.updateEntity(entity, part);

        assertEquals("Updated Part", mapper.toDomain(entity).getName());
        assertEquals(ProductType.PART, entity.getType());
    }

    private Part part() {
        Part part = new Part();
        part.setName("Part");
        part.setSku("P1");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setCostPrice(BigDecimal.ONE);
        part.setSalePrice(BigDecimal.TEN);
        part.setManufacturerCode("M");
        part.setWarrantyMonths(12);
        return part;
    }
}
