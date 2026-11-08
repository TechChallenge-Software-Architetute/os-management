package com.os.workshop.application.saga;

import com.os.workshop.application.notification.OrderStatusNotificationService;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.application.stock.ConfirmReservationUseCase;
import com.os.workshop.application.stock.ReleaseReservationUseCase;
import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.domain.stock.StockReservation;
import com.os.workshop.domain.stock.StockReservationStatus;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandleSagaCommandUseCaseTest {

    @Mock private ServiceOrderRepository serviceOrderRepository;
    @Mock private StockReservationRepository reservationRepository;
    @Mock private ConfirmReservationUseCase confirmReservationUseCase;
    @Mock private ReleaseReservationUseCase releaseReservationUseCase;
    @Mock private OrderStatusNotificationService notificationService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ServiceOrderMetrics metrics;

    @InjectMocks private HandleSagaCommandUseCase useCase;

    private final UUID orderId = UUID.randomUUID();

    // ==================== EXECUTAR_SERVICO ====================

    @Test
    void executarServicoComOsAprovadaIniciaAExecucaoERespondeAoOrquestrador() {
        var order = givenOrder(OrderServiceStatusEnum.APROVADO);
        givenSaveReturnsArgument();

        useCase.execute(orderId, SagaAction.EXECUTAR_SERVICO);

        assertThat(order.getStatus()).isEqualTo(OrderServiceStatusEnum.EM_EXECUCAO);
        verify(serviceOrderRepository).save(order);
        verify(notificationService).notifyStatusChange(order);
        verify(eventPublisher).publishEvent(new OrderSagaCommand(orderId, SagaAction.EXECUTAR_SERVICO));
    }

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class, names = {"EM_EXECUCAO", "FINALIZADA", "ENTREGUE"})
    void executarServicoRepetidoSoReenviaAConfirmacao(OrderServiceStatusEnum status) {
        var order = givenOrder(status);

        useCase.execute(orderId, SagaAction.EXECUTAR_SERVICO);

        assertThat(order.getStatus()).isEqualTo(status);
        verify(serviceOrderRepository, never()).save(any());
        verify(eventPublisher).publishEvent(new OrderSagaCommand(orderId, SagaAction.EXECUTAR_SERVICO));
    }

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class,
            names = {"RECEBIDA", "EM_DIAGNOSTICO", "AGUARDANDO_APROVACAO", "RECUSADA", "CANCELADA"})
    void executarServicoSemOrcamentoAprovadoERecusado(OrderServiceStatusEnum status) {
        var order = givenOrder(status);

        assertThatThrownBy(() -> useCase.execute(orderId, SagaAction.EXECUTAR_SERVICO))
                .isInstanceOf(SagaCommandRejectedException.class)
                .hasMessageContaining(status.name());

        assertThat(order.getStatus()).isEqualTo(status);
        verify(serviceOrderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
        verify(metrics).processingFailed("saga_executar_servico");
    }

    // ==================== ATUALIZAR_ESTOQUE ====================

    @Test
    void atualizarEstoqueConfirmaAsReservasERespondeAoOrquestrador() {
        givenOrder(OrderServiceStatusEnum.EM_EXECUCAO);
        givenActiveReservations();

        useCase.execute(orderId, SagaAction.ATUALIZAR_ESTOQUE);

        verify(confirmReservationUseCase).execute(orderId);
        verify(eventPublisher).publishEvent(new OrderSagaCommand(orderId, SagaAction.ATUALIZAR_ESTOQUE));
    }

    @Test
    void atualizarEstoqueSemReservasAtivasSoResponde() {
        givenOrder(OrderServiceStatusEnum.EM_EXECUCAO);

        useCase.execute(orderId, SagaAction.ATUALIZAR_ESTOQUE);

        verifyNoInteractions(confirmReservationUseCase);
        verify(eventPublisher).publishEvent(new OrderSagaCommand(orderId, SagaAction.ATUALIZAR_ESTOQUE));
    }

    @Test
    void atualizarEstoqueAntesDaExecucaoERecusado() {
        givenOrder(OrderServiceStatusEnum.APROVADO);

        assertThatThrownBy(() -> useCase.execute(orderId, SagaAction.ATUALIZAR_ESTOQUE))
                .isInstanceOf(SagaCommandRejectedException.class);

        verifyNoInteractions(confirmReservationUseCase, eventPublisher);
        verify(metrics).processingFailed("saga_atualizar_estoque");
    }

    // ==================== Compensacao ====================

    @Test
    void negarServicoLiberaAsReservasECancelaAOs() {
        var order = givenOrder(OrderServiceStatusEnum.APROVADO);
        givenActiveReservations();
        givenSaveReturnsArgument();

        useCase.execute(orderId, SagaAction.NEGAR_SERVICO);

        verify(releaseReservationUseCase).execute(orderId);
        assertThat(order.getStatus()).isEqualTo(OrderServiceStatusEnum.CANCELADA);
        assertThat(order.getRejectionReason()).contains("pagamento");
        verify(serviceOrderRepository).save(order);
        verify(notificationService).notifyStatusChange(order);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void negarEstoqueSemReservasSoCancelaAOs() {
        var order = givenOrder(OrderServiceStatusEnum.RECEBIDA);
        givenSaveReturnsArgument();

        useCase.execute(orderId, SagaAction.NEGAR_ESTOQUE);

        verifyNoInteractions(releaseReservationUseCase);
        assertThat(order.getStatus()).isEqualTo(OrderServiceStatusEnum.CANCELADA);
        assertThat(order.getRejectionReason()).isEqualTo("Estoque negado pela saga");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void erroDoOrquestradorTambemCompensa() {
        var order = givenOrder(OrderServiceStatusEnum.EM_DIAGNOSTICO);
        givenSaveReturnsArgument();

        useCase.execute(orderId, SagaAction.ERRO);

        assertThat(order.getStatus()).isEqualTo(OrderServiceStatusEnum.CANCELADA);
        assertThat(order.getRejectionReason()).contains("erro");
    }

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class, names = {"CANCELADA", "RECUSADA"})
    void compensacaoDeOsJaEncerradaNaoFazNada(OrderServiceStatusEnum status) {
        var order = givenOrder(status);

        useCase.execute(orderId, SagaAction.NEGAR_SERVICO);

        assertThat(order.getStatus()).isEqualTo(status);
        verify(serviceOrderRepository, never()).save(any());
        verifyNoInteractions(releaseReservationUseCase, eventPublisher);
    }

    @ParameterizedTest
    @EnumSource(value = OrderServiceStatusEnum.class, names = {"EM_EXECUCAO", "FINALIZADA", "ENTREGUE"})
    void compensacaoDepoisQueAExecucaoComecouERecusada(OrderServiceStatusEnum status) {
        var order = givenOrder(status);

        assertThatThrownBy(() -> useCase.execute(orderId, SagaAction.NEGAR_ESTOQUE))
                .isInstanceOf(SagaCommandRejectedException.class);

        assertThat(order.getStatus()).isEqualTo(status);
        verifyNoInteractions(releaseReservationUseCase, eventPublisher);
        verify(metrics).processingFailed("saga_negar_estoque");
    }

    // ==================== Fora do contrato ====================

    @ParameterizedTest
    @EnumSource(value = SagaAction.class, names = {"CRIAR_OS", "GERAR_ORCAMENTO", "GERAR_PAGAMENTO"})
    void acaoQueNaoEDestinadaAoOsManagementERecusada(SagaAction action) {
        givenOrder(OrderServiceStatusEnum.APROVADO);

        assertThatThrownBy(() -> useCase.execute(orderId, action))
                .isInstanceOf(SagaCommandRejectedException.class)
                .hasMessageContaining("nao e destinada ao os-management");

        verifyNoInteractions(eventPublisher, confirmReservationUseCase, releaseReservationUseCase);
    }

    @Test
    void osInexistenteFalha() {
        when(serviceOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(orderId, SagaAction.EXECUTAR_SERVICO))
                .isInstanceOf(NoSuchElementException.class);

        verifyNoInteractions(eventPublisher);
    }

    // ==================== Apoio ====================

    private ServiceOrder givenOrder(OrderServiceStatusEnum status) {
        var order = ServiceOrder.reconstitute(orderId, "52998224725", "BDD1A23", List.of("TROCA_OLEO"),
                "[TROCA_OLEO]", status, null, LocalDateTime.now(), LocalDateTime.now());
        when(serviceOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        return order;
    }

    private void givenActiveReservations() {
        var reservation = new StockReservation();
        reservation.setStatus(StockReservationStatus.ACTIVE);
        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
    }

    private void givenSaveReturnsArgument() {
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}
