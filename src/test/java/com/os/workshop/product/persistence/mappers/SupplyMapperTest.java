package com.os.workshop.product.persistence.mappers;

import com.os.workshop.features.product.domain.ProductType;
import com.os.workshop.features.product.domain.Supply;
import com.os.workshop.features.product.domain.UnitOfMeasure;
import com.os.workshop.features.product.persistence.mappers.SupplyMapper;
import com.os.workshop.features.product.persistence.mappers.SupplyMapperImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupplyMapperTest {

    private final SupplyMapper mapper = new SupplyMapperImpl();

    @Test
    void convertsAndUpdatesEntity() {
        Supply supply = supply();
        var entity = mapper.toEntity(supply);

        supply.setName("Updated Supply");
        mapper.updateEntity(entity, supply);

        assertEquals("Updated Supply", mapper.toDomain(entity).getName());
        assertEquals(ProductType.SUPPLY, entity.getType());
    }

    private Supply supply() {
        Supply supply = new Supply();
        supply.setName("Supply");
        supply.setSku("S1");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setCostPrice(BigDecimal.ONE);
        supply.setSalePrice(BigDecimal.TEN);
        supply.setFractionalAllowed(true);
        supply.setPackageSize(BigDecimal.ONE);
        return supply;
    }
}
