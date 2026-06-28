package com.os.workshop.application.product.supply;

import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.domain.product.Supply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindSupplyBySkuUseCaseTest {

    @Mock private SupplyRepository supplyRepository;
    @InjectMocks private FindSupplyBySkuUseCase useCase;

    @Test
    void returnsSupplyWhenFound() {
        Supply supply = new Supply(); supply.setSku("OIL-001");
        when(supplyRepository.findBySku("OIL-001")).thenReturn(Optional.of(supply));
        assertEquals("OIL-001", useCase.execute("OIL-001").getSku());
    }

    @Test
    void throwsWhenNotFound() {
        when(supplyRepository.findBySku("NONE")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("NONE"));
    }
}
