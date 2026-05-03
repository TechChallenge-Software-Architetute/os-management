package com.os.workshop.features.product.supply.findBySku;

import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindSupplyBySkuHandlerTest {

    @Mock private SupplyRepository supplyRepository;
    @InjectMocks private FindSupplyBySkuHandler handler;

    @Test
    void returnsSupplyWhenFound() {
        Supply supply = new Supply(); supply.setSku("OIL-001");
        when(supplyRepository.findBySku("OIL-001")).thenReturn(Optional.of(supply));
        assertEquals("OIL-001", handler.handle("OIL-001").getSku());
    }

    @Test
    void throwsWhenNotFound() {
        when(supplyRepository.findBySku("NONE")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> handler.handle("NONE"));
    }
}
