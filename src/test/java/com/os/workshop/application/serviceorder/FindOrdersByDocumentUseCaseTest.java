package com.os.workshop.application.serviceorder;

import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindOrdersByDocumentUseCaseTest {

    @Mock private ServiceOrderRepository serviceOrderRepository;
    @InjectMocks private FindOrdersByDocumentUseCase useCase;

    @Test
    void returnsOrdersForDocument() {
        String doc = "52998224725";
        List<ServiceOrder> orders = List.of(
                ServiceOrder.reconstitute(UUID.randomUUID(), doc, "ABC1D23",
                        List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                        OrderServiceStatusEnum.RECEBIDA, null,
                        LocalDateTime.now(), LocalDateTime.now())
        );
        when(serviceOrderRepository.findByCpfCnpj(doc)).thenReturn(orders);

        List<ServiceOrder> result = useCase.execute(doc);

        assertEquals(1, result.size());
        verify(serviceOrderRepository).findByCpfCnpj(doc);
    }

    @Test
    void returnsEmptyForUnknownDocument() {
        when(serviceOrderRepository.findByCpfCnpj("00000000000")).thenReturn(List.of());

        List<ServiceOrder> result = useCase.execute("00000000000");

        assertTrue(result.isEmpty());
    }

    @Test
    void normalizesFormattedDocument() {
        when(serviceOrderRepository.findByCpfCnpj("52998224725")).thenReturn(List.of());

        useCase.execute("529.982.247-25");

        verify(serviceOrderRepository).findByCpfCnpj("52998224725");
    }
}
