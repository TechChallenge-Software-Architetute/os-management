package com.os.workshop.application.product.part;

import com.os.workshop.application.product.part.port.out.PartRepository;
import com.os.workshop.domain.product.Part;
import com.os.workshop.domain.product.ProductType;
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
class PartServiceTest {

    @Mock
    private PartRepository partRepository;

    @InjectMocks
    private CreatePartUseCase createPartUseCase;

    @InjectMocks
    private FindPartByIdUseCase findPartByIdUseCase;

    @InjectMocks
    private FindPartBySkuUseCase findPartBySkuUseCase;

    @InjectMocks
    private ListPartsUseCase listPartsUseCase;

    @InjectMocks
    private DeactivatePartUseCase deactivatePartUseCase;

    private Part createPart() {
        Part part = new Part();
        part.setId(1L);
        part.setName("Brake Pad");
        part.setSku("BP-001");
        part.setType(ProductType.PART);
        part.setUnit(UnitOfMeasure.UNIT);
        part.setActive(true);
        return part;
    }

    @Test
    void whenCreatingPartWithUniqueSku_thenPartIsSaved() {
        when(partRepository.existsBySku("BP-001")).thenReturn(false);
        when(partRepository.save(any(Part.class))).thenAnswer(i -> i.getArgument(0));

        Part result = createPartUseCase.execute("Brake Pad", "BP-001", UnitOfMeasure.UNIT,
                "Brakes", "Bosch", new BigDecimal("45"), new BigDecimal("90"), "MFG-001", 12);

        assertEquals("Brake Pad", result.getName());
        assertEquals(ProductType.PART, result.getType());
        verify(partRepository).save(any(Part.class));
    }

    @Test
    void whenCreatingPartWithDuplicateSku_thenThrowsIllegalArgument() {
        when(partRepository.existsBySku("BP-001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                createPartUseCase.execute("Brake Pad", "BP-001", UnitOfMeasure.UNIT,
                        "Brakes", "Bosch", new BigDecimal("45"), new BigDecimal("90"), "MFG-001", 12));
    }

    @Test
    void whenFindingPartByExistingId_thenReturnsPart() {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        Part result = findPartByIdUseCase.execute(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void whenFindingPartByNonExistingId_thenThrowsIllegalArgument() {
        when(partRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> findPartByIdUseCase.execute(999L));
    }

    @Test
    void whenFindingPartByExistingSku_thenReturnsPart() {
        Part part = createPart();
        when(partRepository.findBySku("BP-001")).thenReturn(Optional.of(part));

        Part result = findPartBySkuUseCase.execute("BP-001");

        assertEquals("BP-001", result.getSku());
    }

    @Test
    void whenFindingAllParts_thenReturnsOnlyActive() {
        when(partRepository.findAllActive()).thenReturn(List.of(createPart()));

        List<Part> result = listPartsUseCase.execute();

        assertEquals(1, result.size());
    }

    @Test
    void whenDeactivatingActivePart_thenPartIsSetInactive() {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.save(any(Part.class))).thenAnswer(i -> i.getArgument(0));

        deactivatePartUseCase.execute(1L);

        assertFalse(part.isActive());
        verify(partRepository).save(part);
    }

    @Test
    void whenDeactivatingAlreadyInactivePart_thenThrowsIllegalArgument() {
        Part part = createPart();
        part.setActive(false);
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        assertThrows(IllegalArgumentException.class, () -> deactivatePartUseCase.execute(1L));
    }
}
