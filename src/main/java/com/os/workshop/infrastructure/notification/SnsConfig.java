package com.os.workshop.infrastructure.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SnsConfig {

    @Bean
    public SnsClient snsClient(@Value("${aws.sns.region:us-east-1}") String region) {
        return SnsClient.builder()
                .region(Region.of(region))
                .build();
    }
}
