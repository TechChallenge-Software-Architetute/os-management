package com.os.workshop.validators.model;

import com.os.workshop.validators.annotation.Document;
import com.os.workshop.validators.enums.DocumentType;
import jakarta.validation.Payload;
import java.lang.annotation.Annotation;

public record FakeDocument(DocumentType type) implements Document {

    @Override
    public String message() {
        return "Invalid document";
    }

    @Override
    public Class<?>[] groups() {
        return new Class[0];
    }

    @Override
    public Class<? extends Payload>[] payload() {
        return new Class[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Document.class;
    }
}