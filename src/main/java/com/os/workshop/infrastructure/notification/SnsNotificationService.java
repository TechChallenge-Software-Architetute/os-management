package com.os.workshop.infrastructure.notification;

import com.os.workshop.application.notification.port.out.EmailNotificationPort;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
@Slf4j
public class SnsNotificationService implements EmailNotificationPort {

    private final SnsClient snsClient;
    private final String topicArn;
    private final ServiceOrderMetrics metrics;

    public SnsNotificationService(
            SnsClient snsClient,
            @Value("${aws.sns.topic-arn}") String topicArn,
            ServiceOrderMetrics metrics) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
        this.metrics = metrics;
    }

    @Override
    @Async
    public void sendStatusUpdate(String toEmail, String clientName, String vehicleInfo, String newStatus) {
        try {
            String subject = "OS Management - Atualizacao de Status: " + newStatus;
            String message = buildMessage(clientName, vehicleInfo, newStatus);

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(message)
                    .build();

            snsClient.publish(request);
            log.info("SNS notification published - Client: {}, Status: {}", clientName, newStatus);
        } catch (Exception e) {
            metrics.integrationFailed("aws_sns");
            log.error("Failed to publish SNS notification for {}: {}", clientName, e.getMessage(), e);
        }
    }

    private String buildMessage(String clientName, String vehicleInfo, String status) {
        String statusMessage = getStatusMessage(status);

        return String.format("""
                ============================================
                OS Management - Notificacao de Status
                ============================================
                
                Ola %s,
                
                %s
                
                Status atual: %s
                Veiculo: %s
                
                --------------------------------------------
                Oficina Mecanica - OS Management
                Este e um email automatico.
                ============================================
                """, clientName, statusMessage, status, vehicleInfo);
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "Recebida" -> "Sua ordem de servico foi recebida e esta em nossa fila de atendimento.";
            case "Em Diagnostico" -> "Seu veiculo esta sendo avaliado pelo nosso mecanico.";
            case "Aguardando Aprovacao" -> "O diagnostico foi concluido. Por favor, avalie o orcamento para aprovar ou recusar.";
            case "Aprovado" -> "O orcamento foi aprovado! Nosso mecanico iniciara os servicos em breve.";
            case "Em Execucao" -> "Os servicos estao sendo realizados no seu veiculo.";
            case "Finalizada" -> "Todos os servicos foram concluidos! Seu veiculo esta pronto para retirada.";
            case "Entregue" -> "Seu veiculo foi entregue. Obrigado pela confianca!";
            case "Recusada" -> "O orcamento foi recusado. Entre em contato conosco caso deseje reavaliar.";
            default -> "O status da sua ordem de servico foi atualizado para: " + status;
        };
    }
}
