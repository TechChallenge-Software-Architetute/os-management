package com.os.workshop.validators;

import com.os.workshop.validators.enums.DocumentType;
import com.os.workshop.validators.model.FakeDocument;
import com.os.workshop.validators.validator.DocumentValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorsTest {

    private final DocumentValidator validator = new DocumentValidator();

    @Test
    void shouldValidateCpf() {
        validator.initialize(new FakeDocument(DocumentType.CPF));

        assertTrue(validator.isValid("529.982.247-25", null));
        assertTrue(validator.isValid("52998224725", null));
    }

    @Test
    void shouldValidateInvalidCpf() {
        validator.initialize(new FakeDocument(DocumentType.CPF));

        assertFalse(validator.isValid("111.111.111-11", null));
        assertFalse(validator.isValid("123.456.789-00", null));
    }

    @Test
    void shouldValidateCnpj() {
        validator.initialize(new FakeDocument(DocumentType.CNPJ));

        assertTrue(validator.isValid("04.252.011/0001-10", null));
        assertTrue(validator.isValid("04252011000110", null));
    }

    @Test
    void shouldValidateInvalidCnpj() {
        validator.initialize(new FakeDocument(DocumentType.CNPJ));

        assertFalse(validator.isValid("00.000.000/0000-00", null));
        assertFalse(validator.isValid("12345678000100", null));
    }

    @Test
    void shouldReturnFalseWhenNull() {
        validator.initialize(new FakeDocument(DocumentType.CPF));

        assertFalse(validator.isValid(null, null));
    }

    @Test
    void shouldReturnInvalidFormat() {
        validator.initialize(new FakeDocument(DocumentType.CNPJ));

        assertFalse(validator.isValid("123", null));
        assertFalse(validator.isValid("abc", null));
    }
}