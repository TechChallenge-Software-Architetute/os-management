package com.os.workshop.features.service.shared.repository;

import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.infrastructure.persistence.service.ServiceStatusConverter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceStatusConverterTest {

    private final ServiceStatusConverter converter = new ServiceStatusConverter();

    @Test
    void convertsStatusListToJsonAndBack() {
        String json = converter.convertToDatabaseColumn(List.of(new Status(ServiceStatusEnum.DOING, LocalDateTime.now())));
        assertEquals(1, converter.convertToEntityAttribute(json).size());
    }

    @Test
    void handlesEmptyAndInvalidValues() {
        assertEquals("[]", converter.convertToDatabaseColumn(List.of()));
        assertTrue(converter.convertToEntityAttribute("[]").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("not-json"));
    }
}
