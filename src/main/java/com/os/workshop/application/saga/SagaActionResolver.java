package com.os.workshop.application.saga;

import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Traduz uma mudanca de estado da OS na acao da saga que o os-management inicia
 * por conta propria.
 *
 * <p>No contrato com o os-orchestrator (ADR-0006), o os-management so toma a
 * iniciativa uma vez: o {@code CRIAR_OS}, na abertura da OS. Os outros passos sao
 * comandos do orquestrador, e a resposta a cada um sai do
 * {@link HandleSagaCommandUseCase}, nao daqui.
 *
 * <p>Por isso as transicoes de status nao geram comando:
 * <ul>
 *   <li>{@code AGUARDANDO_APROVACAO} deixou de gerar {@code GERAR_ORCAMENTO}: quem
 *       responde essa acao ao orquestrador e o os-payment. Se o os-management
 *       tambem enviasse, o orquestrador receberia a acao em duplicidade e iria
 *       para {@code ERRO}.</li>
 *   <li>{@code EM_EXECUCAO} deixou de gerar {@code EXECUTAR_SERVICO}: a resposta so
 *       sai quando o orquestrador manda executar. Se a oficina iniciar a execucao
 *       pela API, a confirmacao sai quando o comando chegar.</li>
 * </ul>
 *
 * <p>O mapa continua aqui como ponto de extensao: se o contrato ganhar uma acao
 * disparada por transicao de status, e uma linha.
 */
@Component
public class SagaActionResolver {

    /** Estado de destino -> acao da saga. Vazio no contrato atual. */
    private static final Map<OrderServiceStatusEnum, SagaAction> ACTION_BY_TARGET_STATUS = Map.of();

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
