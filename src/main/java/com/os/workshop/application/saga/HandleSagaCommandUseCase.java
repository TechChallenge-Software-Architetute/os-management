package com.os.workshop.application.saga;

import com.os.workshop.application.notification.OrderStatusNotificationService;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.application.stock.ConfirmReservationUseCase;
import com.os.workshop.application.stock.ReleaseReservationUseCase;
import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.domain.stock.StockReservationStatus;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.os.workshop.domain.serviceorder.OrderServiceStatusEnum.CANCELADA;
import static com.os.workshop.domain.serviceorder.OrderServiceStatusEnum.EM_EXECUCAO;
import static com.os.workshop.domain.serviceorder.OrderServiceStatusEnum.ENTREGUE;
import static com.os.workshop.domain.serviceorder.OrderServiceStatusEnum.FINALIZADA;
import static com.os.workshop.domain.serviceorder.OrderServiceStatusEnum.RECUSADA;

/**
 * Aplica no os-management os comandos que o os-orchestrator envia pela fila
 * {@code os-management-<env>-order-events} (contrato no ADR-0006).
 *
 * <ul>
 *   <li>{@code EXECUTAR_SERVICO}: OS aprovada vai para {@code EM_EXECUCAO}.
 *       Responde {@code EXECUTAR_SERVICO}.</li>
 *   <li>{@code ATUALIZAR_ESTOQUE}: confirma (baixa) as reservas da OS.
 *       Responde {@code ATUALIZAR_ESTOQUE}.</li>
 *   <li>{@code NEGAR_SERVICO}, {@code NEGAR_ESTOQUE}, {@code ERRO}: compensacao.
 *       Libera as reservas e cancela a OS. Sem resposta.</li>
 * </ul>
 *
 * <p>Tudo numa transacao. A resposta e publicada como evento interno e so vai
 * para o SQS depois do commit ({@link OrderSagaEventListener}), como o
 * {@code CRIAR_OS}.
 *
 * <p>Comando repetido (o SQS entrega pelo menos uma vez) nao refaz o trabalho: OS
 * ja em execucao so tem a confirmacao reenviada, e OS ja cancelada ou recusada nao
 * e compensada de novo. Comando que nao cabe no estado atual da OS gera
 * {@link SagaCommandRejectedException}, que faz a mensagem acabar na DLQ.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HandleSagaCommandUseCase {

    /** A execucao ja comecou (ou terminou): o servico nao precisa ser liberado de novo. */
    private static final Set<OrderServiceStatusEnum> EXECUTION_STARTED = EnumSet.of(EM_EXECUCAO, FINALIZADA, ENTREGUE);

    /** A OS ja foi encerrada sem execucao: nao ha o que compensar. */
    private static final Set<OrderServiceStatusEnum> ALREADY_COMPENSATED = EnumSet.of(CANCELADA, RECUSADA);

    private final ServiceOrderRepository serviceOrderRepository;
    private final StockReservationRepository reservationRepository;
    private final ConfirmReservationUseCase confirmReservationUseCase;
    private final ReleaseReservationUseCase releaseReservationUseCase;
    private final OrderStatusNotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final ServiceOrderMetrics metrics;

    @Transactional
    public void execute(UUID orderId, SagaAction action) {
        Objects.requireNonNull(orderId, "orderId do comando da saga e obrigatorio.");
        Objects.requireNonNull(action, "action do comando da saga e obrigatoria.");

        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Ordem de servico nao encontrada com id: " + orderId));

        try {
            switch (action) {
                case EXECUTAR_SERVICO -> executeService(order);
                case ATUALIZAR_ESTOQUE -> updateStock(order);
                case NEGAR_SERVICO, NEGAR_ESTOQUE, ERRO -> compensate(order, action);
                default -> throw new SagaCommandRejectedException(orderId, action,
                        "a acao nao e destinada ao os-management");
            }
        } catch (SagaCommandRejectedException e) {
            metrics.processingFailed("saga_" + action.name().toLowerCase(Locale.ROOT));
            throw e;
        }
    }

    private void executeService(ServiceOrder order) {
        OrderServiceStatusEnum status = order.getStatus();

        if (status == OrderServiceStatusEnum.APROVADO) {
            order.advanceTo(EM_EXECUCAO);
            ServiceOrder saved = serviceOrderRepository.save(order);
            notificationService.notifyStatusChange(saved);
            log.info("OS {} liberada pela saga: APROVADO -> EM_EXECUCAO.", order.getId());
        } else if (EXECUTION_STARTED.contains(status)) {
            log.info("OS {} ja esta em {}; reenviando a confirmacao de EXECUTAR_SERVICO.", order.getId(), status);
        } else {
            throw new SagaCommandRejectedException(order.getId(), SagaAction.EXECUTAR_SERVICO,
                    "a OS esta em " + status + "; a execucao so comeca depois do orcamento aprovado");
        }

        reply(order.getId(), SagaAction.EXECUTAR_SERVICO);
    }

    private void updateStock(ServiceOrder order) {
        if (!EXECUTION_STARTED.contains(order.getStatus())) {
            throw new SagaCommandRejectedException(order.getId(), SagaAction.ATUALIZAR_ESTOQUE,
                    "a OS esta em " + order.getStatus() + "; o estoque so e baixado depois que a execucao comeca");
        }

        if (hasActiveReservations(order.getId())) {
            confirmReservationUseCase.execute(order.getId());
            log.info("Reservas de estoque da OS {} confirmadas pela saga.", order.getId());
        } else {
            log.info("OS {} sem reservas ativas para baixar (sem pecas ou ja baixadas).", order.getId());
        }

        reply(order.getId(), SagaAction.ATUALIZAR_ESTOQUE);
    }

    private void compensate(ServiceOrder order, SagaAction action) {
        OrderServiceStatusEnum status = order.getStatus();

        if (ALREADY_COMPENSATED.contains(status)) {
            log.info("OS {} ja esta em {}; {} nao tem o que compensar.", order.getId(), status, action);
            return;
        }
        if (!status.canTransitionTo(CANCELADA)) {
            throw new SagaCommandRejectedException(order.getId(), action,
                    "a OS esta em " + status + " e nao pode mais ser cancelada automaticamente");
        }

        if (hasActiveReservations(order.getId())) {
            releaseReservationUseCase.execute(order.getId());
        }
        order.cancel(cancellationReason(action));
        ServiceOrder saved = serviceOrderRepository.save(order);
        notificationService.notifyStatusChange(saved);
        log.info("OS {} cancelada pela saga ({}).", order.getId(), action);
    }

    private boolean hasActiveReservations(UUID orderId) {
        return !reservationRepository
                .findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE)
                .isEmpty();
    }

    /** A resposta sai como evento interno e so vai para o SQS depois do commit. */
    private void reply(UUID orderId, SagaAction action) {
        eventPublisher.publishEvent(new OrderSagaCommand(orderId, action));
    }

    private static String cancellationReason(SagaAction action) {
        return switch (action) {
            case NEGAR_SERVICO -> "Servico negado pela saga (pagamento nao aprovado)";
            case NEGAR_ESTOQUE -> "Estoque negado pela saga";
            default -> "Saga interrompida por erro no orquestrador";
        };
    }
}
