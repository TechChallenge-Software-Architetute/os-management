package com.os.workshop.adapter.in.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.application.saga.HandleSagaCommandUseCase;
import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.application.saga.SagaCorrelation;
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
 * <p>Formato (ADR-0006): corpo JSON {@code {"order", "action", "amount", "correlationId"}},
 * o mesmo que o os-management envia. A acao vem do campo {@code action} do JSON;
 * o atributo de mensagem {@code action} e o corpo so com o id da OS (formato
 * antigo) continuam aceitos enquanto o orquestrador migra. O correlationId
 * recebido vai para o MDC, e a resposta ao orquestrador sai com ele.
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

    private static final ObjectMapper JSON = new ObjectMapper();

    private final HandleSagaCommandUseCase handleSagaCommandUseCase;

    @SqsListener("${aws.sqs.order-events.queue}")
    public void onMessage(@Payload String body,
                          @Header(name = ACTION_ATTRIBUTE, required = false) String action) {
        InboundMessage message = parseBody(body);
        // O campo action do JSON e o contrato; o atributo e o fallback do formato antigo.
        SagaAction sagaAction = parseAction(message.action() != null ? message.action() : action);

        MDC.put("orderId", message.orderId().toString());
        MDC.put("sagaAction", sagaAction.name());
        if (message.correlationId() != null) {
            MDC.put(SagaCorrelation.MDC_KEY, message.correlationId());
        }
        try {
            log.info("Comando da saga recebido: {} para a OS {}.", sagaAction, message.orderId());
            handleSagaCommandUseCase.execute(message.orderId(), sagaAction);
        } finally {
            MDC.remove("orderId");
            MDC.remove("sagaAction");
            MDC.remove(SagaCorrelation.MDC_KEY);
        }
    }

    /** Id da OS, acao e correlationId extraidos do corpo (acao e correlationId opcionais). */
    record InboundMessage(UUID orderId, String action, String correlationId) {
    }

    static InboundMessage parseBody(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Mensagem da saga sem o id da OS no corpo.");
        }
        String value = body.trim();
        if (value.startsWith("{")) {
            return parseJson(value);
        }
        // Formato antigo: o corpo e so o id da OS, com ou sem aspas.
        if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        return new InboundMessage(parseOrderId(value, body), null, null);
    }

    private static InboundMessage parseJson(String json) {
        JsonNode node;
        try {
            node = JSON.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Corpo da mensagem da saga nao e um JSON valido: " + json, e);
        }
        String order = text(node, "order");
        String action = text(node, "action");
        String correlationId = text(node, "correlationId");
        return new InboundMessage(parseOrderId(order, json), action, correlationId);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private static UUID parseOrderId(String value, String body) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Mensagem da saga sem o id da OS: " + body);
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Corpo da mensagem da saga nao tem um id de OS valido: " + body, e);
        }
    }

    static SagaAction parseAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Mensagem da saga sem a acao (campo ou atributo '" + ACTION_ATTRIBUTE + "').");
        }
        try {
            return SagaAction.valueOf(action.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Acao da saga desconhecida: " + action, e);
        }
    }
}
