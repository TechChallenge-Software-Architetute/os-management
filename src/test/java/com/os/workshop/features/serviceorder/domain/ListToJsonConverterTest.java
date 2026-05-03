package com.os.workshop.features.serviceorder.domain;

import com.os.workshop.features.serviceorder.shared.domain.ListToJsonConverter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListToJsonConverterTest {

    private final ListToJsonConverter converter = new ListToJsonConverter();

    @Test
    void convertsListToJsonAndBack() {
        String json = converter.convertToDatabaseColumn(List.of("REVISAO", "TROCA"));

        assertEquals(List.of("REVISAO", "TROCA"), converter.convertToEntityAttribute(json));
    }

    @Test
    void returnsEmptyForNullEmptyAndInvalidValues() {
        assertEquals("[]", converter.convertToDatabaseColumn(null));
        assertTrue(converter.convertToEntityAttribute(null).isEmpty());
        assertTrue(converter.convertToEntityAttribute("not-json").isEmpty());
    }
}
