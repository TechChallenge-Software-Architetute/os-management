package com.os.workshop.product.supply;

import com.os.workshop.product.domain.ProductType;
import com.os.workshop.product.domain.Supply;
import com.os.workshop.product.domain.UnitOfMeasure;
import com.os.workshop.product.repository.SupplyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyServiceTest {

    @Mock
    private SupplyRepository supplyRepository;

    @InjectMocks
    private SupplyService supplyService;

    private SupplyRequest createRequest() {
        return new SupplyRequest("Engine Oil", "OIL-5W30", UnitOfMeasure.LITER,
                "Lubricants", "Mobil", new BigDecimal("25"), new BigDecimal("50"), true, new BigDecimal("1"));
    }

    private Supply createSupply() {
        Supply supply = new Supply();
        supply.setId(1L);
        supply.setName("Engine Oil");
        supply.setSku("OIL-5W30");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setActive(true);
        supply.setFractionalAllowed(true);
        return supply;
    }

    @Test
    void whenCreatingSupplyWithUniqueSku_thenSupplyIsSaved() {
        when(supplyRepository.existsBySku("OIL-5W30")).thenReturn(false);
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> i.getArgument(0));

        Supply result = supplyService.create(createRequest());

        assertEquals("Engine Oil", result.getName());
        assertEquals(ProductType.SUPPLY, result.getType());
        verify(supplyRepository).save(any(Supply.class));
    }

    @Test
    void whenCreatingSupplyWithDuplicateSku_thenThrowsIllegalArgument() {
        when(supplyRepository.existsBySku("OIL-5W30")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> supplyService.create(createRequest()));
    }

    @Test
    void whenFindingSupplyByExistingId_thenReturnsSupply() {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));

        Supply result = supplyService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void whenFindingSupplyByNonExistingId_thenThrowsIllegalArgument() {
        when(supplyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> supplyService.findById(999L));
    }

    @Test
    void whenDeactivatingActiveSupply_thenSupplyIsSetInactive() {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> i.getArgument(0));

        supplyService.deactivate(1L);

        assertFalse(supply.isActive());
        verify(supplyRepository).save(supply);
    }

    @Test
    void whenDeactivatingAlreadyInactiveSupply_thenThrowsIllegalArgument() {
        Supply supply = createSupply();
        supply.setActive(false);
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));

        assertThrows(IllegalArgumentException.class, () -> supplyService.deactivate(1L));
    }

    @Test
    void whenFindingAllSupplies_thenReturnsOnlyActive() {
        when(supplyRepository.findAllActive()).thenReturn(List.of(createSupply()));

        assertEquals(1, supplyService.findAll().size());
    }
}
