package com.os.workshop.vehicle.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LicensePlateTest {

    @Test
    void whenCreatingWithOldFormat_thenValueIsNormalized() {
        LicensePlate plate = new LicensePlate("ABC-1234");

        assertEquals("ABC1234", plate.getValue());
    }

    @Test
    void whenCreatingWithMercosulFormat_thenValueIsNormalized() {
        LicensePlate plate = new LicensePlate("abc1d23");

        assertEquals("ABC1D23", plate.getValue());
    }

    @Test
    void whenCreatingWithLowerCase_thenValueIsUpperCase() {
        LicensePlate plate = new LicensePlate("abc1234");

        assertEquals("ABC1234", plate.getValue());
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
        LicensePlate plate = new LicensePlate("ABC1234");

        assertEquals("ABC-1234", plate.formatted());
    }
}
