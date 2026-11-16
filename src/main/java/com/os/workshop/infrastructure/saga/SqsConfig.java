package com.os.workshop.infrastructure.saga;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

/**
 * Cliente SQS. Credenciais vêm do default provider chain (role dos nodes do EKS
 * ou variáveis de ambiente do pod) — nada fixo no código, no mesmo formato do
 * {@code SnsConfig}.
 */
@Configuration
public class SqsConfig {

    @Bean
    public SqsClient sqsClient(@Value("${aws.sqs.region:us-east-1}") String region,
                               @Value("${aws.sqs.endpoint:}") String endpoint) {
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.of(region));
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder.build();
    }

    SqsClient sqsClient(String region) {
        return sqsClient(region, "");
    }
}
