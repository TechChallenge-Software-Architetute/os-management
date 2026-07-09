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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyServiceTest {

    @Mock
    private SupplyRepository supplyRepository;

    @InjectMocks
    private CreateSupplyUseCase createSupplyUseCase;

    @InjectMocks
    private FindSupplyByIdUseCase findSupplyByIdUseCase;

    @InjectMocks
    private ListSuppliesUseCase listSuppliesUseCase;

    @InjectMocks
    private DeactivateSupplyUseCase deactivateSupplyUseCase;

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

        Supply result = createSupplyUseCase.execute("Engine Oil", "OIL-5W30", UnitOfMeasure.LITER,
                "Lubricants", "Mobil", new BigDecimal("25"), new BigDecimal("50"), true, new BigDecimal("1"));

        assertEquals("Engine Oil", result.getName());
        assertEquals(ProductType.SUPPLY, result.getType());
        verify(supplyRepository).save(any(Supply.class));
    }

    @Test
    void whenCreatingSupplyWithDuplicateSku_thenThrowsIllegalArgument() {
        when(supplyRepository.existsBySku("OIL-5W30")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                createSupplyUseCase.execute("Engine Oil", "OIL-5W30", UnitOfMeasure.LITER,
                        "Lubricants", "Mobil", new BigDecimal("25"), new BigDecimal("50"), true, new BigDecimal("1")));
    }

    @Test
    void whenFindingSupplyByExistingId_thenReturnsSupply() {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));

        Supply result = findSupplyByIdUseCase.execute(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void whenFindingSupplyByNonExistingId_thenThrowsIllegalArgument() {
        when(supplyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> findSupplyByIdUseCase.execute(999L));
    }

    @Test
    void whenDeactivatingActiveSupply_thenSupplyIsSetInactive() {
        Supply supply = createSupply();
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> i.getArgument(0));

        deactivateSupplyUseCase.execute(1L);

        assertFalse(supply.isActive());
        verify(supplyRepository).save(supply);
    }

    @Test
    void whenDeactivatingAlreadyInactiveSupply_thenThrowsIllegalArgument() {
        Supply supply = createSupply();
        supply.setActive(false);
        when(supplyRepository.findById(1L)).thenReturn(Optional.of(supply));

        assertThrows(IllegalArgumentException.class, () -> deactivateSupplyUseCase.execute(1L));
    }

    @Test
    void whenFindingAllSupplies_thenReturnsOnlyActive() {
        when(supplyRepository.findAllActive()).thenReturn(List.of(createSupply()));

        assertEquals(1, listSuppliesUseCase.execute().size());
    }
}
