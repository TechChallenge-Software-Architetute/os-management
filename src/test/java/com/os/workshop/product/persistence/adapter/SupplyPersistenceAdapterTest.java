package com.os.workshop.product.persistence.adapter;

import com.os.workshop.product.domain.ProductType;
import com.os.workshop.product.domain.Supply;
import com.os.workshop.product.domain.UnitOfMeasure;
import com.os.workshop.product.persistence.entity.SupplyEntity;
import com.os.workshop.product.persistence.mappers.SupplyMapper;
import com.os.workshop.product.persistence.repository.SupplyJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyPersistenceAdapterTest {

    @Mock
    private SupplyJpaRepository jpaRepository;

    @Mock
    private SupplyMapper mapper;

    @InjectMocks
    private SupplyPersistenceAdapter adapter;

    @Test
    void savesAndQueriesSupplies() {
        Supply supply = supply();
        SupplyEntity entity = new SupplyEntity();

        when(jpaRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(supply);
        when(jpaRepository.findByActiveTrue()).thenReturn(List.of(entity));
        when(jpaRepository.findBySku("S1")).thenReturn(Optional.of(entity));
        when(jpaRepository.existsBySku("S1")).thenReturn(true);

        assertEquals(2L, adapter.save(supply).getId());
        assertTrue(adapter.findById(2L).isPresent());
        assertEquals(1, adapter.findAllActive().size());
        assertTrue(adapter.findBySku("S1").isPresent());
        assertTrue(adapter.existsBySku("S1"));
    }

    private Supply supply() {
        Supply supply = new Supply();
        supply.setId(2L);
        supply.setSku("S1");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setCostPrice(BigDecimal.ONE);
        supply.setSalePrice(BigDecimal.TEN);
        return supply;
    }
}
