package com.os.workshop.features.product.supply.update;

import com.os.workshop.features.product.shared.domain.ProductType;
import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSupplyHandlerTest {

    @Mock private SupplyRepository supplyRepository;
    @InjectMocks private UpdateSupplyHandler handler;

    private Supply createSupply() {
        Supply s = new Supply(); s.setId(1L); s.setName("Oil"); s.setSku("OIL-001");
        s.setType(ProductType.SUPPLY); s.setUnit(UnitOfMeasure.LITER);
        s.setCostPrice(BigDecimal.TEN); s.setSalePrice(new BigDecimal("20"));
        return s;
    }

    @Test
    void updatesSupplySuccessfully() {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> i.getArgument(0));
        var request = new UpdateSupplyRequest("Updated", "OIL-001", UnitOfMeasure.LITER, "Cat", "Brand", BigDecimal.TEN, new BigDecimal("20"), true, BigDecimal.ONE);
        assertEquals("Updated", handler.handle(1L, request).getName());
    }

    @Test
    void throwsWhenSupplyNotFound() {
        when(supplyRepository.findById(99L)).thenReturn(Optional.empty());
        var request = new UpdateSupplyRequest("X", "X", UnitOfMeasure.LITER, null, null, BigDecimal.ONE, BigDecimal.ONE, false, null);
        assertThrows(IllegalArgumentException.class, () -> handler.handle(99L, request));
    }

    @Test
    void throwsWhenDuplicateSku() {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));
        when(supplyRepository.existsBySku("NEW")).thenReturn(true);
        var request = new UpdateSupplyRequest("X", "NEW", UnitOfMeasure.LITER, null, null, BigDecimal.ONE, BigDecimal.ONE, false, null);
        assertThrows(IllegalArgumentException.class, () -> handler.handle(1L, request));
    }
}
