package com.os.workshop.serviceorder.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateOrderRequestTest {

    @Test
    void storesStatus() {
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setStatus(OrderServiceStatusEnum.APROVADO);

        assertEquals(OrderServiceStatusEnum.APROVADO, request.getStatus());
    }
}
