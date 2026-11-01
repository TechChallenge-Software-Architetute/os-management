package com.os.workshop.infrastructure.saga;

import com.os.workshop.application.saga.OrderSagaCommand;
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
 * Entrega comandos na fila de entrada do os-orchestrator.
 *
 * <p>A URL da fila é determinística
 * ({@code os-orchestrator-<env>-order-processing}) e chega pelo ConfigMap, não
 * por remote state do Terraform — é isso que mantém os deploys independentes.
 *
 * <p>O formato da mensagem é o que o {@code StateMachineListener} do orquestrador
 * já espera: corpo = id da ordem, atributo {@code action} = nome da ação.
 */
@Component
@Slf4j
public class SqsSagaCommandPublisher implements SagaCommandPublisherPort {

    private static final String ACTION_ATTRIBUTE = "action";

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
            // Visível no startup em vez de silencioso: o deploy local (Kind) não
            // tem fila, e sem esse aviso a saga ficaria desligada sem ninguém notar.
            log.warn("aws.sqs.orchestrator-queue-url nao configurada "
                    + "(AWS_SQS_ORCHESTRATOR_QUEUE_URL). A saga esta DESLIGADA: "
                    + "nenhum comando sera enviado ao os-orchestrator.");
        }
    }

    @Override
    public void publish(OrderSagaCommand command) {
        if (isDisabled()) {
            return;
        }

        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(command.orderId().toString())
                    .messageAttributes(Map.of(ACTION_ATTRIBUTE, MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(command.action().name())
                            .build()))
                    .build());

            log.info("Comando {} enviado ao orquestrador para a ordem {}.",
                    command.action(), command.orderId());
        } catch (RuntimeException e) {
            // Mesmo tratamento do SnsNotificationService: a falha de integração é
            // registrada e medida, mas não desfaz a mudança de estado já commitada
            // nem propaga erro para o cliente HTTP.
            metrics.integrationFailed("aws_sqs");
            log.error("Falha ao enviar o comando {} da ordem {} ao orquestrador: {}",
                    command.action(), command.orderId(), e.getMessage(), e);
        }
    }

    private boolean isDisabled() {
        return queueUrl.isEmpty();
    }
}
