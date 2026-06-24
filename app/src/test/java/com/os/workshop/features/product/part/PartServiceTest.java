package com.os.workshop.features.product.part;

import com.os.workshop.features.product.part.create.CreatePartHandler;
import com.os.workshop.features.product.part.create.CreatePartRequest;
import com.os.workshop.features.product.part.deactivate.DeactivatePartHandler;
import com.os.workshop.features.product.part.findById.FindPartByIdHandler;
import com.os.workshop.features.product.part.findBySku.FindPartBySkuHandler;
import com.os.workshop.features.product.part.list.ListPartsHandler;
import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.domain.ProductType;
import com.os.workshop.features.product.shared.domain.UnitOfMeasure;
import com.os.workshop.features.product.shared.repository.PartRepository;
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
    private CreatePartHandler createPartHandler;

    @InjectMocks
    private FindPartByIdHandler findPartByIdHandler;

    @InjectMocks
    private FindPartBySkuHandler findPartBySkuHandler;

    @InjectMocks
    private ListPartsHandler listPartsHandler;

    @InjectMocks
    private DeactivatePartHandler deactivatePartHandler;

    private CreatePartRequest createRequest() {
        return new CreatePartRequest("Brake Pad", "BP-001", UnitOfMeasure.UNIT,
                "Brakes", "Bosch", new BigDecimal("45"), new BigDecimal("90"), "MFG-001", 12);
    }

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

        Part result = createPartHandler.handle(createRequest());

        assertEquals("Brake Pad", result.getName());
        assertEquals(ProductType.PART, result.getType());
        verify(partRepository).save(any(Part.class));
    }

    @Test
    void whenCreatingPartWithDuplicateSku_thenThrowsIllegalArgument() {
        when(partRepository.existsBySku("BP-001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> createPartHandler.handle(createRequest()));
    }

    @Test
    void whenFindingPartByExistingId_thenReturnsPart() {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        Part result = findPartByIdHandler.handle(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void whenFindingPartByNonExistingId_thenThrowsIllegalArgument() {
        when(partRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> findPartByIdHandler.handle(999L));
    }

    @Test
    void whenFindingPartByExistingSku_thenReturnsPart() {
        Part part = createPart();
        when(partRepository.findBySku("BP-001")).thenReturn(Optional.of(part));

        Part result = findPartBySkuHandler.handle("BP-001");

        assertEquals("BP-001", result.getSku());
    }

    @Test
    void whenFindingAllParts_thenReturnsOnlyActive() {
        when(partRepository.findAllActive()).thenReturn(List.of(createPart()));

        List<Part> result = listPartsHandler.handle();

        assertEquals(1, result.size());
    }

    @Test
    void whenDeactivatingActivePart_thenPartIsSetInactive() {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.save(any(Part.class))).thenAnswer(i -> i.getArgument(0));

        deactivatePartHandler.handle(1L);

        assertFalse(part.isActive());
        verify(partRepository).save(part);
    }

    @Test
    void whenDeactivatingAlreadyInactivePart_thenThrowsIllegalArgument() {
        Part part = createPart();
        part.setActive(false);
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));

        assertThrows(IllegalArgumentException.class, () -> deactivatePartHandler.handle(1L));
    }
}
