package com.os.workshop.infrastructure.saga;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.assertj.core.api.Assertions.assertThat;

class SqsConfigTest {

    @Test
    void criaClienteNaRegiaoConfigurada() {
        try (var client = new SqsConfig().sqsClient("sa-east-1")) {
            assertThat(client).isNotNull();
            assertThat(client.serviceClientConfiguration().region()).isEqualTo(Region.SA_EAST_1);
        }
    }
}
