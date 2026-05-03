package com.os.workshop.service.domain.requests;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateServiceRequestTest {

    @Test
    void storesRequestValues() {
        UUID id = UUID.randomUUID();
        CreateServiceRequest request = new CreateServiceRequest();
        request.setServiceType("REVISAO");
        request.setIdOS(id);

        assertEquals("REVISAO", request.getServiceType());
        assertEquals(id, request.getIdOS());
    }
}
