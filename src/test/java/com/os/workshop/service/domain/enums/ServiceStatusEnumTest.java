package com.os.workshop.service.domain.enums;

import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceStatusEnumTest {

    @Test
    void exposesStatusValue() {
        assertEquals("TO_DO", ServiceStatusEnum.TO_DO.getStatus());
    }
}
