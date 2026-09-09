package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.notification.OrderStatusNotificationService;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.serviceorder.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecideOrderUseCaseTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private OrderStatusNotificationService notificationService;
    @InjectMocks private DecideOrderUseCase useCase;

    private static final String CLIENT_EMAIL = "joao@email.com";
    private static final String CLIENT_DOC = "52998224725";
    private static final UUID ORDER_ID = UUID.randomUUID();

    private Client createClient() {
        return Client.reconstitute(1L, "JOAO", CLIENT_DOC, CLIENT_EMAIL, "11999999999",
                true, LocalDateTime.now(), LocalDateTime.now());
    }

    private ServiceOrder createOrderAguardando() {
        return ServiceOrder.reconstitute(ORDER_ID, CLIENT_DOC, "ABC1D23",
                List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                OrderServiceStatusEnum.AGUARDANDO_APROVACAO, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void approvesOrderSuccessfully() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(createClient()));
        when(serviceOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(createOrderAguardando()));
        when(serviceOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(CLIENT_EMAIL, ORDER_ID, Decision.APPROVED, null);

        verify(serviceOrderRepository).save(any());
        verify(notificationService).notifyStatusChange(any());
        verify(eventPublisher, never()).publishEvent(any(OrderRejectedEvent.class));
    }

    @Test
    void resolvesClientByCpfSubjectFromServerlessToken() {
        when(clientRepository.findByDocument(CLIENT_DOC)).thenReturn(Optional.of(createClient()));
        when(serviceOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(createOrderAguardando()));
        when(serviceOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(CLIENT_DOC, ORDER_ID, Decision.APPROVED, null);

        verify(serviceOrderRepository).save(any());
        verify(notificationService).notifyStatusChange(any());
    }

    @Test
    void rejectsOrderSuccessfully() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(createClient()));
        when(serviceOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(createOrderAguardando()));
        when(serviceOrderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(CLIENT_EMAIL, ORDER_ID, Decision.REJECTED, "Preco alto");

        verify(serviceOrderRepository).save(any());
        verify(eventPublisher).publishEvent(any(OrderRejectedEvent.class));
        verify(notificationService).notifyStatusChange(any());
    }

    @Test
    void throwsWhenClientNotFound() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class,
                () -> useCase.execute(CLIENT_EMAIL, ORDER_ID, Decision.APPROVED, null));

        verify(serviceOrderRepository, never()).save(any());
    }

    @Test
    void throwsWhenOrderNotFound() {
        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(createClient()));
        when(serviceOrderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> useCase.execute(CLIENT_EMAIL, ORDER_ID, Decision.APPROVED, null));
    }

    @Test
    void throwsWhenOrderDoesNotBelongToClient() {
        Client client = createClient();
        ServiceOrder orderFromOtherClient = ServiceOrder.reconstitute(ORDER_ID, "99999999999", "XYZ9A88",
                List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                OrderServiceStatusEnum.AGUARDANDO_APROVACAO, null,
                LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(client));
        when(serviceOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderFromOtherClient));

        assertThrows(SecurityException.class,
                () -> useCase.execute(CLIENT_EMAIL, ORDER_ID, Decision.APPROVED, null));
    }

    @Test
    void throwsWhenInvalidTransition() {
        ServiceOrder orderRecebida = ServiceOrder.reconstitute(ORDER_ID, CLIENT_DOC, "ABC1D23",
                List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                OrderServiceStatusEnum.RECEBIDA, null,
                LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findByEmail(CLIENT_EMAIL)).thenReturn(Optional.of(createClient()));
        when(serviceOrderRepository.findById(ORDER_ID)).thenReturn(Optional.of(orderRecebida));

        assertThrows(InvalidStatusTransitionException.class,
                () -> useCase.execute(CLIENT_EMAIL, ORDER_ID, Decision.APPROVED, null));
    }
}
