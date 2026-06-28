package com.os.workshop.features.service.update;

import com.os.workshop.adapter.in.web.service.UpdateServiceRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateServiceRequestTest {

    @Test
    void storesRequestValues() {
        UUID id = UUID.randomUUID();
        UpdateServiceRequest request = new UpdateServiceRequest();
        request.setServiceType("TROCA");
        request.setIdOS(id);

        assertEquals("TROCA", request.getServiceType());
        assertEquals(id, request.getIdOS());
    }
}
