package com.os.workshop.serviceorder.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderServiceStatusEnumTest {

    @Test
    void exposesStatusValue() {
        assertEquals("RECEBIDA", OrderServiceStatusEnum.RECEBIDA.getStatus());
    }
}
