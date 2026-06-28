package com.os.workshop.features.vehicle.shared.domain.valueobject;

import com.os.workshop.domain.vehicle.LicensePlate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LicensePlateTest {

    @Test
    void whenCreatingWithOldFormat_thenValueIsNormalized() {
        assertEquals("ABC1234", new LicensePlate("ABC-1234").getValue());
    }

    @Test
    void whenCreatingWithMercosulFormat_thenValueIsNormalized() {
        assertEquals("ABC1D23", new LicensePlate("abc1d23").getValue());
    }

    @Test
    void whenCreatingWithLowerCase_thenValueIsUpperCase() {
        assertEquals("ABC1234", new LicensePlate("abc1234").getValue());
    }

    @Test
    void whenCreatingWithNull_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new LicensePlate(null));
    }

    @Test
    void whenCreatingWithBlank_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new LicensePlate(" "));
    }

    @Test
    void whenCreatingWithInvalidFormat_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new LicensePlate("12345"));
    }

    @Test
    void whenFormattingOldPlate_thenHasHyphen() {
        assertEquals("ABC-1234", new LicensePlate("ABC1234").formatted());
    }
}
