package com.os.workshop.infrastructure.saga;

import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.application.saga.SagaMessage;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsSagaCommandPublisherTest {

    private static final String QUEUE_URL =
            "https://sqs.us-east-1.amazonaws.com/123456789012/os-orchestrator-develop-order-processing";

    @Mock private SqsClient sqsClient;
    @Mock private ServiceOrderMetrics metrics;

    private final UUID orderId = UUID.randomUUID();

    @Test
    void enviaUuidNoCorpoEAcaoValorECorrelationIdComoAtributos() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, QUEUE_URL);

        publisher.publish(message(SagaAction.CRIAR_OS, new BigDecimal("179.8")));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        SendMessageRequest request = captor.getValue();

        assertThat(request.messageBody()).isEqualTo(orderId.toString());

        assertThat(request.queueUrl()).isEqualTo(QUEUE_URL);
        assertThat(request.messageAttributes().get("action").dataType()).isEqualTo("String");
        assertThat(request.messageAttributes().get("action").stringValue()).isEqualTo("CRIAR_OS");
        assertThat(request.messageAttributes().get("amount").stringValue()).isEqualTo("179.80");
        assertThat(request.messageAttributes().get("correlationId").stringValue()).isEqualTo("corr-1");
        verifyNoInteractions(metrics);
    }

    @Test
    void removeEspacosDaUrlConfigurada() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, "  " + QUEUE_URL + "  ");

        publisher.publish(message(SagaAction.ATUALIZAR_ESTOQUE, BigDecimal.ONE));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertThat(captor.getValue().queueUrl()).isEqualTo(QUEUE_URL);
    }

    @Test
    void semUrlConfiguradaASagaFicaDesligada() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, "");

        publisher.warnWhenDisabled();
        publisher.publish(message(SagaAction.CRIAR_OS, BigDecimal.ONE));

        verifyNoInteractions(sqsClient, metrics);
    }

    @Test
    void urlNulaTambemDesligaASaga() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, null);

        publisher.publish(message(SagaAction.CRIAR_OS, BigDecimal.ONE));

        verifyNoInteractions(sqsClient, metrics);
    }

    @Test
    void comUrlConfiguradaNaoAvisaNoStartup() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, QUEUE_URL);

        assertThatNoException().isThrownBy(publisher::warnWhenDisabled);
        verifyNoInteractions(sqsClient);
    }

    @Test
    void falhaNoSqsEMedidaENaoPropaga() {
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("SQS indisponivel"));
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, QUEUE_URL);

        assertThatNoException().isThrownBy(() -> publisher.publish(message(SagaAction.EXECUTAR_SERVICO, BigDecimal.TEN)));

        verify(metrics).integrationFailed("aws_sqs");
    }

    private SagaMessage message(SagaAction action, BigDecimal amount) {
        return new SagaMessage(orderId, action, amount, "corr-1");
    }
}
