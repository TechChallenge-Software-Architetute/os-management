package com.os.workshop.application.product.supply;

import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.domain.product.ProductType;
import com.os.workshop.domain.product.Supply;
import com.os.workshop.domain.product.UnitOfMeasure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSupplyUseCaseTest {

    @Mock private SupplyRepository supplyRepository;
    @InjectMocks private UpdateSupplyUseCase useCase;

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
        assertEquals("Updated", useCase.execute(1L, "Updated", "OIL-001", UnitOfMeasure.LITER, "Cat", "Brand", BigDecimal.TEN, new BigDecimal("20"), true, BigDecimal.ONE).getName());
    }

    @Test
    void throwsWhenSupplyNotFound() {
        when(supplyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(99L, "X", "X", UnitOfMeasure.LITER, null, null, BigDecimal.ONE, BigDecimal.ONE, false, null));
    }

    @Test
    void throwsWhenDuplicateSku() {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));
        when(supplyRepository.existsBySku("NEW")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L, "X", "NEW", UnitOfMeasure.LITER, null, null, BigDecimal.ONE, BigDecimal.ONE, false, null));
    }
}
