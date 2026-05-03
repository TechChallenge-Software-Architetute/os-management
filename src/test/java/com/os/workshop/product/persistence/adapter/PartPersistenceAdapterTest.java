package com.os.workshop.product.persistence.adapter;

import com.os.workshop.product.domain.Part;
import com.os.workshop.product.domain.ProductType;
import com.os.workshop.product.domain.UnitOfMeasure;
import com.os.workshop.product.persistence.entity.PartEntity;
import com.os.workshop.product.persistence.mappers.PartMapper;
import com.os.workshop.product.persistence.repository.PartJpaRepository;
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
class PartPersistenceAdapterTest {

    @Mock
    private PartJpaRepository jpaRepository;

    @Mock
    private PartMapper mapper;

    @InjectMocks
    private PartPersistenceAdapter adapter;

    @Test
    void savesAndQueriesParts() {
        Part part = part();
        PartEntity entity = new PartEntity();

        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(part);
        when(jpaRepository.findByActiveTrue()).thenReturn(List.of(entity));
        when(jpaRepository.findBySku("P1")).thenReturn(Optional.of(entity));
        when(jpaRepository.existsBySku("P1")).thenReturn(true);

        assertEquals(1L, adapter.save(part).getId());
        assertTrue(adapter.findById(1L).isPresent());
        assertEquals(1, adapter.findAllActive().size());
        assertTrue(adapter.findBySku("P1").isPresent());
        assertTrue(adapter.existsBySku("P1"));
    }

    private Part part() {
        Part part = new Part();
        part.setId(1L);
        part.setSku("P1");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setCostPrice(BigDecimal.ONE);
        part.setSalePrice(BigDecimal.TEN);
        return part;
    }
}
