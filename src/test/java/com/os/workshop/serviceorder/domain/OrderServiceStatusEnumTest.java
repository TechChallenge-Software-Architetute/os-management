package com.os.workshop.serviceorder.domain;

import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderServiceStatusEnumTest {

    @Test
    void exposesStatusValue() {
        assertEquals("RECEBIDA", OrderServiceStatusEnum.RECEBIDA.getStatus());
    }
}
