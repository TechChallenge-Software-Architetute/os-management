package com.os.workshop.features.service.updateStatus;

import com.os.workshop.adapter.in.web.service.UpdateServiceStatusRequest;
import com.os.workshop.domain.service.ServiceStatusEnum;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateServiceStatusRequestTest {

    @Test
    void storesRequestValues() {
        UUID id = UUID.randomUUID();
        UpdateServiceStatusRequest request = new UpdateServiceStatusRequest();
        request.setId(id);
        request.setStatus(ServiceStatusEnum.DONE);

        assertEquals(id, request.getId());
        assertEquals(ServiceStatusEnum.DONE, request.getStatus());
    }
}
