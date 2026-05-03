package com.os.workshop.service.domain.requests;

import com.os.workshop.service.domain.enums.ServiceStatusEnum;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateStatusServiceRequestTest {

    @Test
    void storesRequestValues() {
        UUID id = UUID.randomUUID();
        UpdateStatusServiceRequest request = new UpdateStatusServiceRequest();
        request.setId(id);
        request.setStatus(ServiceStatusEnum.DONE);

        assertEquals(id, request.getId());
        assertEquals(ServiceStatusEnum.DONE, request.getStatus());
    }
}
