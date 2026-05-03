package com.os.workshop.client.dto;

import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.dto.ClientResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientResponseTest {

    @Test
    void formatsCpfFromClient() {
        LocalDateTime now = LocalDateTime.now();
        Client client = Client.reconstitute(1L, "ANA", "52998224725", "ana@email.com", "9999", true, now, now);

        ClientResponse response = ClientResponse.from(client);

        assertEquals("529.982.247-25", response.cpf());
        assertTrue(response.active());
    }
}
