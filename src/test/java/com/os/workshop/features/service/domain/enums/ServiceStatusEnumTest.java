package com.os.workshop.features.service.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceStatusEnumTest {

    @Test
    void exposesStatusValue() {
        assertEquals("TO_DO", ServiceStatusEnum.TO_DO.getStatus());
    }
}
