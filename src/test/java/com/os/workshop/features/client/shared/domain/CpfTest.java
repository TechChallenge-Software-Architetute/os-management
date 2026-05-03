package com.os.workshop.features.client.shared.domain;

import com.os.workshop.features.client.shared.domain.valueobject.Cpf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfTest {

    private static final String VALID_CPF_RAW = "52998224725";
    private static final String VALID_CPF_FORMATTED = "529.982.247-25";

    @Test
    void whenCreatingCpfWithValidRawDigits_thenStoresNormalizedValue() {
        Cpf cpf = new Cpf(VALID_CPF_RAW);

        assertEquals(VALID_CPF_RAW, cpf.getValue());
    }

    @Test
    void whenCreatingCpfWithFormattedInput_thenNormalizesToDigitsOnly() {
        Cpf cpf = new Cpf(VALID_CPF_FORMATTED);

        assertEquals(VALID_CPF_RAW, cpf.getValue());
    }

    @Test
    void whenFormattingCpf_thenReturnsOfficialBrazilianFormat() {
        Cpf cpf = new Cpf(VALID_CPF_RAW);

        assertEquals(VALID_CPF_FORMATTED, cpf.formatted());
    }

    @Test
    void whenCreatingCpfWithNull_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf(null));
    }

    @Test
    void whenCreatingCpfWithTooFewDigits_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("1234567890"));
    }

    @Test
    void whenCreatingCpfWithTooManyDigits_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("123456789012"));
    }

    @Test
    void whenCreatingCpfWithAllIdenticalDigits_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("11111111111"));
    }

    @Test
    void whenCreatingCpfWithInvalidFirstCheckDigit_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("52998224735"));
    }

    @Test
    void whenCreatingCpfWithInvalidSecondCheckDigit_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new Cpf("52998224726"));
    }

    @Test
    void whenComparingTwoCpfsWithSameValue_thenTheyAreEqual() {
        Cpf cpf1 = new Cpf(VALID_CPF_RAW);
        Cpf cpf2 = new Cpf(VALID_CPF_FORMATTED);

        assertEquals(cpf1, cpf2);
        assertEquals(cpf1.hashCode(), cpf2.hashCode());
    }

    @Test
    void whenComparingTwoCpfsWithDifferentValues_thenTheyAreNotEqual() {
        Cpf cpf1 = new Cpf(VALID_CPF_RAW);
        Cpf cpf2 = new Cpf("39053344705");

        assertNotEquals(cpf1, cpf2);
    }

    @Test
    void whenCallingToString_thenReturnsFormattedCpf() {
        Cpf cpf = new Cpf(VALID_CPF_RAW);

        assertEquals(VALID_CPF_FORMATTED, cpf.toString());
    }
}
