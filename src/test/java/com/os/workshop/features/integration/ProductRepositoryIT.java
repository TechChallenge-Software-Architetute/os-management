package com.os.workshop.features.integration;

import com.os.workshop.domain.product.ProductType;
import com.os.workshop.domain.product.UnitOfMeasure;
import com.os.workshop.infrastructure.persistence.product.PartEntity;
import com.os.workshop.infrastructure.persistence.product.PartJpaRepository;
import com.os.workshop.infrastructure.persistence.product.SupplyEntity;
import com.os.workshop.infrastructure.persistence.product.SupplyJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the JOINED inheritance strategy between ProductEntity, PartEntity, and SupplyEntity.
 * This is one of the most complex JPA mappings — if inheritance is misconfigured,
 * queries will fail or return wrong types.
 */
class ProductRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private PartJpaRepository partJpaRepository;

    @Autowired
    private SupplyJpaRepository supplyJpaRepository;

    @Test
    void savesAndRetrievesPart() {
        PartEntity part = new PartEntity();
        part.setName("Brake Pad");
        part.setSku("BP-INT-001");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setCostPrice(new BigDecimal("45.00"));
        part.setSalePrice(new BigDecimal("90.00"));
        part.setManufacturerCode("MFG-001");
        part.setWarrantyMonths(12);

        PartEntity saved = partJpaRepository.save(part);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        var found = partJpaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Brake Pad", found.get().getName());
        assertEquals(ProductType.PART, found.get().getType());
        assertEquals(12, found.get().getWarrantyMonths());
    }

    @Test
    void savesAndRetrievesSupply() {
        SupplyEntity supply = new SupplyEntity();
        supply.setName("Engine Oil 5W30");
        supply.setSku("OIL-INT-001");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setCostPrice(new BigDecimal("25.00"));
        supply.setSalePrice(new BigDecimal("50.00"));
        supply.setFractionalAllowed(true);
        supply.setPackageSize(new BigDecimal("1.0"));

        SupplyEntity saved = supplyJpaRepository.save(supply);

        assertNotNull(saved.getId());

        var found = supplyJpaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(ProductType.SUPPLY, found.get().getType());
        assertTrue(found.get().isFractionalAllowed());
    }

    @Test
    void findsBySkuAndActiveStatus() {
        PartEntity part = new PartEntity();
        part.setName("Filter");
        part.setSku("FLT-INT-001");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setCostPrice(BigDecimal.TEN);
        part.setSalePrice(new BigDecimal("20"));
        part.setWarrantyMonths(6);
        part.setActive(true);

        partJpaRepository.save(part);

        assertTrue(partJpaRepository.findBySku("FLT-INT-001").isPresent());
        assertTrue(partJpaRepository.existsBySku("FLT-INT-001"));
        assertFalse(partJpaRepository.findByActiveTrue().isEmpty());
    }
}
