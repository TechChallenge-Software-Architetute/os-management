package com.os.workshop.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class EmailTemplateRenderer {

    private static final String TEMPLATE_PATH = "templates/status-update-email.html";
    private final String template;

    public EmailTemplateRenderer() {
        this.template = loadTemplate();
    }

    public String render(String clientName, String vehicleInfo, String status) {
        String statusMessage = getStatusMessage(status);

        return template
                .replace("{{clientName}}", clientName)
                .replace("{{vehicleInfo}}", vehicleInfo)
                .replace("{{status}}", status)
                .replace("{{statusMessage}}", statusMessage);
    }

    private String loadTemplate() {
        try {
            var resource = new ClassPathResource(TEMPLATE_PATH);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template from {}: {}", TEMPLATE_PATH, e.getMessage());
            return getFallbackTemplate();
        }
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

    private String getFallbackTemplate() {
        return """
                <html><body>
                <h2>OS Management - Atualizacao de Status</h2>
                <p>Ola {{clientName}},</p>
                <p>{{statusMessage}}</p>
                <p><strong>Veiculo:</strong> {{vehicleInfo}}</p>
                <p><strong>Status atual:</strong> {{status}}</p>
                <p>Atenciosamente,<br/>Oficina Mecanica</p>
                </body></html>
                """;
    }
}
