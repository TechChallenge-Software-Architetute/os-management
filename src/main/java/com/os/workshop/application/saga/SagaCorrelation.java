package com.os.workshop.application.saga;

import com.os.workshop.infrastructure.monitoring.RequestCorrelationFilter;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * De onde vem o {@code correlationId} das mensagens da saga.
 *
 * <p>Ordem de preferencia, lida no contexto de log (MDC) da thread que envia:
 * <ol>
 *   <li>{@link #MDC_KEY}: o correlationId que chegou do orquestrador, quando a
 *       mensagem e a resposta a um comando dele. Assim a saga inteira da OS
 *       mantem o mesmo id entre os servicos.</li>
 *   <li>{@code request_id} do {@link RequestCorrelationFilter}: a requisicao HTTP
 *       que abriu a OS. Liga o log da API ao log da saga.</li>
 *   <li>Um UUID novo, se nenhum dos dois existir.</li>
 * </ol>
 * O envio acontece em AFTER_COMMIT, na mesma thread do use case, entao o MDC da
 * requisicao (ou da mensagem) ainda esta disponivel.
 */
public final class SagaCorrelation {

    /** Mesma chave usada no MDC do os-orchestrator e do os-payment. */
    public static final String MDC_KEY = "correlationId";

    private SagaCorrelation() {
    }

    public static String current() {
        String fromSaga = MDC.get(MDC_KEY);
        if (fromSaga != null && !fromSaga.isBlank()) {
            return fromSaga;
        }
        String fromRequest = MDC.get(RequestCorrelationFilter.REQUEST_ID);
        if (fromRequest != null && !fromRequest.isBlank()) {
            return fromRequest;
        }
        return UUID.randomUUID().toString();
    }
}
