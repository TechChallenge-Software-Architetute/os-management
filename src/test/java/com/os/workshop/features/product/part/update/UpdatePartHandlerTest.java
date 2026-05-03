package com.os.workshop.features.product.part.update;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePartHandlerTest {

    @Mock private PartRepository partRepository;
    @InjectMocks private UpdatePartHandler handler;

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
        var request = new UpdatePartRequest("Updated", "BP-001", UnitOfMeasure.UNIT, "Cat", "Brand", BigDecimal.TEN, new BigDecimal("20"), "MFG", 12);
        var result = handler.handle(1L, request);
        assertEquals("Updated", result.getName());
    }

    @Test
    void throwsWhenPartNotFound() {
        when(partRepository.findById(99L)).thenReturn(Optional.empty());
        var request = new UpdatePartRequest("X", "X", UnitOfMeasure.UNIT, null, null, BigDecimal.ONE, BigDecimal.ONE, null, 0);
        assertThrows(IllegalArgumentException.class, () -> handler.handle(99L, request));
    }

    @Test
    void throwsWhenDuplicateSku() {
        Part part = createPart();
        when(partRepository.findById(1L)).thenReturn(Optional.of(part));
        when(partRepository.existsBySku("NEW-SKU")).thenReturn(true);
        var request = new UpdatePartRequest("X", "NEW-SKU", UnitOfMeasure.UNIT, null, null, BigDecimal.ONE, BigDecimal.ONE, null, 0);
        assertThrows(IllegalArgumentException.class, () -> handler.handle(1L, request));
    }
}
