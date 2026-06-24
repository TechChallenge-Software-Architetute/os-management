package com.os.workshop.features.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.os.workshop.features.utils.annotations.UpperCase;

import java.io.IOException;

public class UpperCaseDeserializer extends JsonDeserializer<String>
        implements ContextualDeserializer {

    private boolean enabled;

    public UpperCaseDeserializer() {
        this.enabled = false;
    }

    public UpperCaseDeserializer(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        String value = p.getValueAsString();
        if (!enabled || value == null) {
            return value;
        }
        return value.trim().toUpperCase();
    }

    @Override
    public JsonDeserializer<?> createContextual(
            DeserializationContext ctxt,
            BeanProperty property
    ) {

        if (property != null &&
            property.getAnnotation(UpperCase.class) != null) {
            return new UpperCaseDeserializer(true);
        }

        return new UpperCaseDeserializer(false);
    }
}
