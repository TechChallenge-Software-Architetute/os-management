package com.os.workshop.bdd;

import com.os.workshop.application.notification.port.out.EmailNotificationPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Substitui as duas integracoes com a AWS nos cenarios BDD.
 *
 * <p>Os comandos que sairiam para a fila do os-orchestrator sao capturados em
 * memoria: o BDD verifica o contrato do lado do os-management (qual acao sai,
 * para qual OS, em que ordem), nao a entrega pela AWS. O e-mail via SNS vira
 * no-op para o teste nao tentar resolver credenciais nem sair para a rede.
 */
@TestConfiguration(proxyBeanMethods = false)
public class BddTestConfig {

    @Bean
    @Primary
    RecordingSagaCommandPublisher recordingSagaCommandPublisher() {
        return new RecordingSagaCommandPublisher();
    }

    @Bean
    @Primary
    EmailNotificationPort noOpEmailNotificationPort() {
        return (toEmail, clientName, vehicleInfo, newStatus) -> { };
    }
}
