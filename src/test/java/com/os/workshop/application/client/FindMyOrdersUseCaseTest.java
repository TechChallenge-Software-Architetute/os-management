package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindMyOrdersUseCaseTest {

    private static final String DOC = "52998224725";

    @Mock private ClientRepository clientRepository;
    @Mock private ServiceOrderRepository serviceOrderRepository;
    @InjectMocks private FindMyOrdersUseCase useCase;

    private Client client() {
        return Client.reconstitute(1L, "JOAO", DOC, "joao@email.com", "11999999999",
                true, LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceOrder order() {
        return ServiceOrder.reconstitute(UUID.randomUUID(), DOC, "ABC1D23",
                List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                OrderServiceStatusEnum.RECEBIDA, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void resolvesClientByCpfFromServerlessToken() {
        when(clientRepository.findByDocument(DOC)).thenReturn(Optional.of(client()));
        when(serviceOrderRepository.findByCpfCnpj(DOC)).thenReturn(List.of(order(), order()));

        assertThat(useCase.execute("529.982.247-25")).hasSize(2);
    }

    @Test
    void fallsBackToEmailForLegacyStaffLogin() {
        when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(client()));
        when(serviceOrderRepository.findByCpfCnpj(DOC)).thenReturn(List.of());

        assertThat(useCase.execute("joao@email.com")).isEmpty();
    }

    @Test
    void throwsWhenClientCannotBeResolved() {
        when(clientRepository.findByDocument(anyString())).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> useCase.execute(DOC));
    }
}
