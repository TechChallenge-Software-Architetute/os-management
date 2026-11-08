package com.os.workshop.adapter.in.messaging;

import com.os.workshop.application.saga.HandleSagaCommandUseCase;
import com.os.workshop.application.saga.SagaAction;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consumidor da fila de entrada da saga ({@code os-management-<env>-order-events},
 * a "sqs-gerar-os"), onde o os-orchestrator publica os comandos para o
 * os-management.
 *
 * <p>Formato da mensagem, o mesmo nos dois sentidos (ADR-0006): corpo = id da OS;
 * atributo {@code action} = nome da acao ({@link SagaAction}).
 *
 * <p>So existe com {@code aws.sqs.order-events.enabled=true} (ConfigMap do deploy
 * AWS). No deploy local (Kind) e nos testes nao ha SQS, e um listener sem fila
 * derrubaria o startup.
 *
 * <p>Erros sobem de proposito: a mensagem nao e confirmada, volta para a fila e,
 * depois do {@code maxReceiveCount}, vai para a DLQ.
 */
@Component
@ConditionalOnProperty(name = "aws.sqs.order-events.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OrderEventsSqsListener {

    static final String ACTION_ATTRIBUTE = "action";

    private final HandleSagaCommandUseCase handleSagaCommandUseCase;

    @SqsListener("${aws.sqs.order-events.queue}")
    public void onMessage(@Payload String body,
                          @Header(name = ACTION_ATTRIBUTE, required = false) String action) {
        UUID orderId = parseOrderId(body);
        SagaAction sagaAction = parseAction(action);

        MDC.put("orderId", orderId.toString());
        MDC.put("sagaAction", sagaAction.name());
        try {
            log.info("Comando da saga recebido: {} para a OS {}.", sagaAction, orderId);
            handleSagaCommandUseCase.execute(orderId, sagaAction);
        } finally {
            MDC.remove("orderId");
            MDC.remove("sagaAction");
        }
    }

    static UUID parseOrderId(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Mensagem da saga sem o id da OS no corpo.");
        }
        String value = body.trim();
        // Tolera o id serializado como string JSON ("...").
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Corpo da mensagem da saga nao e um id de OS valido: " + body, e);
        }
    }

    static SagaAction parseAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Mensagem da saga sem o atributo '" + ACTION_ATTRIBUTE + "'.");
        }
        try {
            return SagaAction.valueOf(action.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Acao da saga desconhecida: " + action, e);
        }
    }
}
