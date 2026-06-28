package com.os.workshop.features.service.create;

import com.os.workshop.adapter.in.web.service.CreateServiceRequest;
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
