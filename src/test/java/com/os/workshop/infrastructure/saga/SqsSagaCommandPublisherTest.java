package com.os.workshop.infrastructure.saga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    void enviaCorpoJsonComExatamenteOsQuatroCampos() throws Exception {
        var publisher = new SqsSagaCommandPublisher(sqsClient, metrics, QUEUE_URL);

        publisher.publish(message(SagaAction.CRIAR_OS, new BigDecimal("179.8")));

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        SendMessageRequest request = captor.getValue();

        JsonNode body = new ObjectMapper().readTree(request.messageBody());
        assertThat(body.get("order").asText()).isEqualTo(orderId.toString());
        assertThat(body.get("action").asText()).isEqualTo("CRIAR_OS");
        assertThat(body.get("amount").decimalValue()).isEqualByComparingTo("179.80");
        assertThat(body.get("correlationId").asText()).isEqualTo("corr-1");
        assertThat(body.size()).as("o corpo leva so os 4 campos do contrato").isEqualTo(4);

        assertThat(request.queueUrl()).isEqualTo(QUEUE_URL);
        // Atributo mantido so por compatibilidade com o listener antigo do orquestrador.
        assertThat(request.messageAttributes().get("action").dataType()).isEqualTo("String");
        assertThat(request.messageAttributes().get("action").stringValue()).isEqualTo("CRIAR_OS");
        verifyNoInteractions(metrics);
    }

    @Test
    void valorSaiComDuasCasasNoJson() {
        assertThat(SqsSagaCommandPublisher.toJson(message(SagaAction.EXECUTAR_SERVICO, BigDecimal.ZERO)))
                .isEqualTo("{\"order\":\"" + orderId + "\",\"action\":\"EXECUTAR_SERVICO\","
                        + "\"amount\":0.00,\"correlationId\":\"corr-1\"}");
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
