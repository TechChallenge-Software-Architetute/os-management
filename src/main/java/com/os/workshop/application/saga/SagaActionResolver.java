package com.os.workshop.application.saga;

import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Traduz uma mudanca de estado da OS na acao da saga que o os-management inicia
 * por conta propria.
 *
 * <p>No contrato com o os-orchestrator (ADR-0006), o os-management inicia a saga
 * com {@code CRIAR_OS}, e depois informa a aprovacao do orçamento pelo cliente.
 * As respostas aos comandos do orquestrador saem do
 * {@link HandleSagaCommandUseCase}, nao daqui.
 *
 * <p>A unica transicao que inicia um passo adicional e a aprovacao do cliente:
 * ela informa o orquestrador para que a execucao aguarde também a aprovacao do
 * pagamento. As demais respostas continuam sendo disparadas pelo comando recebido.
 *
 * <p>As transicoes de status sem acao correspondente nao geram comando:
 * <ul>
 *   <li>{@code AGUARDANDO_APROVACAO} deixou de gerar {@code GERAR_ORCAMENTO}: quem
 *       responde essa acao ao orquestrador e o os-payment. Se o os-management
 *       tambem enviasse, o orquestrador receberia a acao em duplicidade.</li>
 *   <li>{@code EM_EXECUCAO} deixou de gerar {@code EXECUTAR_SERVICO}: a resposta so
 *       sai quando o orquestrador manda executar. Se a oficina iniciar a execucao
 *       pela API, a confirmacao sai quando o comando chegar.</li>
 * </ul>
 *
 * <p>O mapa associa transicoes de status às ações que iniciam ou liberam passos da saga.
 */
@Component
public class SagaActionResolver {

    /** Estado de destino -> acao da saga. */
    private static final Map<OrderServiceStatusEnum, SagaAction> ACTION_BY_TARGET_STATUS =
            Map.of(OrderServiceStatusEnum.APROVADO, SagaAction.ORCAMENTO_APROVADO);

    /** A abertura da OS e sempre o primeiro passo da saga. */
    private static final SagaAction CREATION_ACTION = SagaAction.CRIAR_OS;

    public Optional<SagaAction> resolve(OrderSagaEvent event) {
        if (event.isCreation()) {
            return Optional.of(CREATION_ACTION);
        }

        return parseStatus(event.newStatus())
                .map(ACTION_BY_TARGET_STATUS::get);
    }

    private Optional<OrderServiceStatusEnum> parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(OrderServiceStatusEnum.valueOf(status));
        } catch (IllegalArgumentException e) {
            // Estado desconhecido nao derruba o fluxo da OS; so nao ha comando a enviar.
            return Optional.empty();
        }
    }
}
