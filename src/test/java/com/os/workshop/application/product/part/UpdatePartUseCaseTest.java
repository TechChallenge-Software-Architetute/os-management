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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePartUseCaseTest {

    @Mock private PartRepository partRepository;
    @InjectMocks private UpdatePartUseCase useCase;

    private Part createPart() {
        Part p = new Part(); p.setId(1L); p.setName("Pad"); p.setSku("BP-001");
        p.setType(ProductType.PART); p.setUnit(UnitOfMeasure.UNIT);
        p.setCostPrice(BigDecimal.TEN); p.setSalePrice(new BigDecimal("20"));
        return p;
    }

    @Test
    void updatesPartSuccessfully() {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.save(any(Part.class))).thenAnswer(i -> i.getArgument(0));
        var result = useCase.execute(1L, "Updated", "BP-001", UnitOfMeasure.UNIT, "Cat", "Brand", BigDecimal.TEN, new BigDecimal("20"), "MFG", 12);
        assertEquals("Updated", result.getName());
    }

    @Test
    void throwsWhenPartNotFound() {
        when(partRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(99L, "X", "X", UnitOfMeasure.UNIT, null, null, BigDecimal.ONE, BigDecimal.ONE, null, 0));
    }

    @Test
    void throwsWhenDuplicateSku() {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.existsBySku("NEW-SKU")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L, "X", "NEW-SKU", UnitOfMeasure.UNIT, null, null, BigDecimal.ONE, BigDecimal.ONE, null, 0));
    }
}
