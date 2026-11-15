package com.os.workshop.infrastructure.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.application.saga.SagaMessage;
import com.os.workshop.application.saga.port.out.SagaCommandPublisherPort;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entrega as mensagens da saga na fila de entrada do os-orchestrator.
 *
 * <p>A URL da fila e deterministica ({@code os-orchestrator-<env>-order-processing})
 * e chega pelo ConfigMap, nao por remote state do Terraform: e isso que mantem os
 * deploys independentes.
 *
 * <p>Formato (ADR-0006): corpo JSON com exatamente quatro campos,
 * {@code {"order": "<id da OS>", "action": "CRIAR_OS", "amount": 179.80, "correlationId": "..."}}.
 * O atributo de mensagem {@code action} continua sendo enviado com o mesmo valor,
 * so por compatibilidade com o listener antigo do orquestrador enquanto ele migra
 * para o JSON; o contrato e o corpo.
 */
@Component
@Slf4j
public class SqsSagaCommandPublisher implements SagaCommandPublisherPort {

    static final String ACTION_ATTRIBUTE = "action";

    // Jackson 2, o mesmo dos conversores JPA do projeto. Instancia propria: o corpo
    // tem tres campos fixos e nao deve mudar se a configuracao do Jackson da API mudar.
    private static final ObjectMapper JSON = new ObjectMapper();

    private final SqsClient sqsClient;
    private final ServiceOrderMetrics metrics;
    private final String queueUrl;

    public SqsSagaCommandPublisher(SqsClient sqsClient,
                                   ServiceOrderMetrics metrics,
                                   @Value("${aws.sqs.orchestrator-queue-url:}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.metrics = metrics;
        this.queueUrl = queueUrl == null ? "" : queueUrl.trim();
    }

    @PostConstruct
    void warnWhenDisabled() {
        if (isDisabled()) {
            // Visivel no startup em vez de silencioso: o deploy local (Kind) nao
            // tem fila, e sem esse aviso a saga ficaria desligada sem ninguem notar.
            log.warn("aws.sqs.orchestrator-queue-url nao configurada "
                    + "(AWS_SQS_ORCHESTRATOR_QUEUE_URL). A saga esta DESLIGADA: "
                    + "nenhuma mensagem sera enviada ao os-orchestrator.");
        }
    }

    @Override
    public void publish(SagaMessage message) {
        if (isDisabled()) {
            return;
        }

        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(toJson(message))
                    .messageAttributes(Map.of(ACTION_ATTRIBUTE, MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(message.action().name())
                            .build()))
                    .build());

            log.info("Mensagem {} enviada ao orquestrador: OS {}, valor {}, correlationId {}.",
                    message.action(), message.orderId(), message.amount(), message.correlationId());
        } catch (RuntimeException e) {
            // Mesmo tratamento do SnsNotificationService: a falha de integracao e
            // registrada e medida, mas nao desfaz a mudanca de estado ja commitada
            // nem propaga erro para o cliente HTTP.
            metrics.integrationFailed("aws_sqs");
            log.error("Falha ao enviar a mensagem {} da OS {} ao orquestrador: {}",
                    message.action(), message.orderId(), e.getMessage(), e);
        }
    }

    static String toJson(SagaMessage message) {
        // Exatamente estes 4 campos: e o contrato combinado com o os-orchestrator.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("order", message.orderId().toString());
        body.put("action", message.action().name());
        body.put("amount", message.amount());
        body.put("correlationId", message.correlationId());
        try {
            return JSON.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Nao foi possivel serializar a mensagem da saga.", e);
        }
    }

    private boolean isDisabled() {
        return queueUrl.isEmpty();
    }
}
