package com.os.workshop.infrastructure.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class SesConfig {

    @Bean
    public SesClient sesClient(@Value("${aws.ses.region:us-east-1}") String region) {
        return SesClient.builder()
                .region(Region.of(region))
                .build();
    }
}
