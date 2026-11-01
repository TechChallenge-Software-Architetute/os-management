package com.os.workshop.infrastructure.saga;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Cliente SQS. Credenciais vêm do default provider chain (role dos nodes do EKS
 * ou variáveis de ambiente do pod) — nada fixo no código, no mesmo formato do
 * {@code SnsConfig}.
 */
@Configuration
public class SqsConfig {

    @Bean
    public SqsClient sqsClient(@Value("${aws.sqs.region:us-east-1}") String region) {
        return SqsClient.builder()
                .region(Region.of(region))
                .build();
    }
}
