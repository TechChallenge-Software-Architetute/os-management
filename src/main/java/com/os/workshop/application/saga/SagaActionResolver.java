package com.os.workshop.application.saga;

import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Traduz uma mudança de estado da ordem no comando correspondente da saga.
 *
 * <p>O os-management tem oito estados e o orquestrador tem três ações, então a
 * maioria das transições não gera comando algum. O mapa abaixo é a única coisa
 * que precisa mudar quando o orquestrador ganhar ações novas: a chave é o estado
 * de destino, porque o domínio já garante que a transição é válida
 * ({@code ServiceOrder.advanceTo}), e o valor é a ação a enviar.
 */
@Component
public class SagaActionResolver {

    /**
     * Estado de destino -> ação da saga. Transição ausente = nenhum comando.
     *
     * <p>Entradas futuras, quando o orquestrador tiver os passos finais:
     * <ul>
     *   <li>{@code FINALIZADA} -> finalizar serviço</li>
     *   <li>{@code ENTREGUE} -> entregar ordem</li>
     *   <li>{@code RECUSADA} -> recusar orçamento</li>
     * </ul>
     * Hoje {@code APROVADO} e {@code RECUSADA} também não geram comando: a
     * aprovação do cliente só vira {@code EXECUTAR_SERVICO} quando a oficina
     * move a ordem para {@code EM_EXECUCAO}.
     */
    private static final Map<OrderServiceStatusEnum, SagaAction> ACTION_BY_TARGET_STATUS = Map.of(
            OrderServiceStatusEnum.AGUARDANDO_APROVACAO, SagaAction.GERAR_ORCAMENTO,
            OrderServiceStatusEnum.EM_EXECUCAO, SagaAction.EXECUTAR_SERVICO
    );

    /** A criação da ordem é sempre o primeiro passo da saga. */
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
            // Estado desconhecido não derruba o fluxo da ordem; simplesmente não
            // há comando a enviar.
            return Optional.empty();
        }
    }
}
