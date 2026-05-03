package com.os.workshop.utils.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.features.utils.annotations.UpperCase;
import com.os.workshop.features.utils.json.UpperCaseDeserializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UpperCaseDeserializerTest {

    @Test
    void uppercasesWhenEnabled() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        var parser = mapper.createParser("\"abc\"");
        parser.nextToken();

        assertEquals("ABC", new UpperCaseDeserializer(true).deserialize(parser, null));
        assertNotNull(UpperCase.class);
    }

    @Test
    void keepsValueWhenDisabled() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        var parser = mapper.createParser("\"abc\"");
        parser.nextToken();

        assertEquals("abc", new UpperCaseDeserializer(false).deserialize(parser, null));
    }
}
