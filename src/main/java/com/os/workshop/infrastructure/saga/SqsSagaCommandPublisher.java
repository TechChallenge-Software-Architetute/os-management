package com.os.workshop.infrastructure.saga;

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

import java.util.Map;

/**
 * Entrega as mensagens da saga na fila de entrada do os-orchestrator.
 *
 * <p>A URL da fila e deterministica ({@code os-orchestrator-<env>-order-processing})
 * e chega pelo ConfigMap, nao por remote state do Terraform: e isso que mantem os
 * deploys independentes.
 *
 * <p>O corpo é o UUID da OS. A ação, o valor e o correlationId são atributos SQS,
 * conforme o contrato consumido pelo StateMachineListener do os-orchestrator.
 */
@Component
@Slf4j
public class SqsSagaCommandPublisher implements SagaCommandPublisherPort {

    static final String ACTION_ATTRIBUTE = "action";
    static final String AMOUNT_ATTRIBUTE = "amount";
    static final String CORRELATION_ID_ATTRIBUTE = "correlationId";

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
                    .messageBody(message.orderId().toString())
                    .messageAttributes(Map.of(
                            ACTION_ATTRIBUTE, stringAttribute(message.action().name()),
                            AMOUNT_ATTRIBUTE, stringAttribute(message.amount().toPlainString()),
                            CORRELATION_ID_ATTRIBUTE, stringAttribute(message.correlationId())))
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

    private static MessageAttributeValue stringAttribute(String value) {
        return MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(value)
                .build();
    }

    private boolean isDisabled() {
        return queueUrl.isEmpty();
    }
}
