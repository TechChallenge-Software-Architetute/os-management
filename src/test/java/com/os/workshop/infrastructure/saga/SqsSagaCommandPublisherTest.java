package com.os.workshop.infrastructure.saga;

import com.os.workshop.application.saga.OrderSagaCommand;
import com.os.workshop.application.saga.SagaAction;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

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
    void enviaCorpoComIdDaOsEAcaoNoAtributo() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, QUEUE_URL);

        publisher.publish(new OrderSagaCommand(orderId, SagaAction.CRIAR_OS));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        SendMessageRequest request = captor.getValue();

        assertThat(request.queueUrl()).isEqualTo(QUEUE_URL);
        assertThat(request.messageBody()).isEqualTo(orderId.toString());
        assertThat(request.messageAttributes()).containsKey("action");
        assertThat(request.messageAttributes().get("action").dataType()).isEqualTo("String");
        assertThat(request.messageAttributes().get("action").stringValue()).isEqualTo("CRIAR_OS");
        verifyNoInteractions(metrics);
    }

    @Test
    void removeEspacosDaUrlConfigurada() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, "  " + QUEUE_URL + "  ");

        publisher.publish(new OrderSagaCommand(orderId, SagaAction.ATUALIZAR_ESTOQUE));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertThat(captor.getValue().queueUrl()).isEqualTo(QUEUE_URL);
    }

    @Test
    void semUrlConfiguradaASagaFicaDesligada() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, "");

        publisher.warnWhenDisabled();
        publisher.publish(new OrderSagaCommand(orderId, SagaAction.CRIAR_OS));

        verifyNoInteractions(sqsClient, metrics);
    }

    @Test
    void urlNulaTambemDesligaASaga() {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, null);

        publisher.publish(new OrderSagaCommand(orderId, SagaAction.CRIAR_OS));

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

        assertThatNoException().isThrownBy(() ->
                publisher.publish(new OrderSagaCommand(orderId, SagaAction.EXECUTAR_SERVICO)));

        verify(metrics).integrationFailed("aws_sqs");
    }
}
