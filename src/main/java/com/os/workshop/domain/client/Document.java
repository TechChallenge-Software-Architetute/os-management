package com.os.workshop.domain.client;

import com.os.workshop.domain.shared.Cpf;

import java.util.regex.Pattern;

public final class Document {

    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");

    private final String value;
    private final DocumentType type;

    private Document(String value, DocumentType type) {
        this.value = value;
        this.type = type;
    }

    public static Document of(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        String normalized = NON_DIGIT.matcher(raw).replaceAll("");

        return switch (normalized.length()) {
            case 11 -> {
                Cpf cpf = new Cpf(raw);
                yield new Document(cpf.getValue(), DocumentType.CPF);
            }
            case 14 -> {
                Cnpj cnpj = new Cnpj(raw);
                yield new Document(cnpj.getValue(), DocumentType.CNPJ);
            }
            default -> throw new IllegalArgumentException(
                    "Document must have 11 digits (CPF) or 14 digits (CNPJ), got: " + normalized.length());
        };
    }

    public String getValue() {
        return value;
    }

    public DocumentType getType() {
        return type;
    }

    public String formatted() {
        return switch (type) {
            case CPF -> new Cpf(value).formatted();
            case CNPJ -> new Cnpj(value).formatted();
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document other)) return false;
        return value.equals(other.value) && type == other.type;
    }

    @Override
    public int hashCode() {
        return value.hashCode() * 31 + type.hashCode();
    }

    @Override
    public String toString() {
        return formatted();
    }
}
