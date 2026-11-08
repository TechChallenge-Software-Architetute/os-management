package com.os.workshop.infrastructure.notification;

import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnsNotificationServiceTest {

    private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:123456789012:os-management";

    @Mock private SnsClient snsClient;
    @Mock private ServiceOrderMetrics metrics;

    private SnsNotificationService service;

    @BeforeEach
    void setUp() {
        service = new SnsNotificationService(snsClient, TOPIC_ARN, metrics);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Recebida             | esta em nossa fila de atendimento",
            "Em Diagnostico       | esta sendo avaliado pelo nosso mecanico",
            "Aguardando Aprovacao | avalie o orcamento para aprovar ou recusar",
            "Aprovado             | O orcamento foi aprovado",
            "Em Execucao          | estao sendo realizados no seu veiculo",
            "Finalizada           | pronto para retirada",
            "Entregue             | Seu veiculo foi entregue",
            "Recusada             | O orcamento foi recusado",
            "Cancelada            | Sua ordem de servico foi cancelada",
            "Outro Status         | foi atualizado para: Outro Status"
    })
    void publicaNoTopicoAMensagemDoStatus(String status, String expectedText) {
        service.sendStatusUpdate("cliente@test.com", "JOAO", "Toyota Corolla (ABC1D23)", status);

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());
        PublishRequest request = captor.getValue();

        assertThat(request.topicArn()).isEqualTo(TOPIC_ARN);
        assertThat(request.subject()).isEqualTo("OS Management - Atualizacao de Status: " + status);
        assertThat(request.message())
                .contains("Ola JOAO", expectedText, "Status atual: " + status, "Toyota Corolla (ABC1D23)");
        verifyNoInteractions(metrics);
    }

    @Test
    void falhaNoSnsEMedidaENaoPropaga() {
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("SNS indisponivel"));

        assertThatNoException().isThrownBy(() ->
                service.sendStatusUpdate("cliente@test.com", "JOAO", "ABC1D23", "Recebida"));

        verify(metrics).integrationFailed("aws_sns");
    }
}
