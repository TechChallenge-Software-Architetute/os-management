package com.os.workshop.features.stock.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock();
        stock.setId(1L);
        stock.setProductId(1L);
        stock.setQuantity(new BigDecimal("100"));
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setMinimumQuantity(new BigDecimal("10"));
        stock.recalculateAvailableQuantity();
    }

    @Test
    void whenAddingPositiveQuantity_thenQuantityAndAvailableIncrease() {
        stock.addQuantity(new BigDecimal("50"));

        assertEquals(new BigDecimal("150"), stock.getQuantity());
        assertEquals(new BigDecimal("150"), stock.getAvailableQuantity());
    }

    @Test
    void whenAddingZeroOrNegativeQuantity_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stock.addQuantity(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> stock.addQuantity(new BigDecimal("-5")));
    }

    @Test
    void whenRemovingValidQuantity_thenQuantityAndAvailableDecrease() {
        stock.removeQuantity(new BigDecimal("30"));

        assertEquals(new BigDecimal("70"), stock.getQuantity());
        assertEquals(new BigDecimal("70"), stock.getAvailableQuantity());
    }

    @Test
    void whenRemovingMoreThanAvailable_thenThrowsIllegalState() {
        stock.reserve(new BigDecimal("90"));

        assertThrows(IllegalStateException.class, () -> stock.removeQuantity(new BigDecimal("20")));
    }

    @Test
    void whenRemovingZeroOrNegativeQuantity_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stock.removeQuantity(BigDecimal.ZERO));
    }

    @Test
    void whenReservingValidQuantity_thenReservedIncreasesAndAvailableDecreases() {
        stock.reserve(new BigDecimal("40"));

        assertEquals(new BigDecimal("100"), stock.getQuantity());
        assertEquals(new BigDecimal("40"), stock.getReservedQuantity());
        assertEquals(new BigDecimal("60"), stock.getAvailableQuantity());
    }

    @Test
    void whenReservingMoreThanAvailable_thenThrowsIllegalState() {
        assertThrows(IllegalStateException.class, () -> stock.reserve(new BigDecimal("101")));
    }

    @Test
    void whenReservingZeroOrNegativeQuantity_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stock.reserve(BigDecimal.ZERO));
    }

    @Test
    void whenReleasingReservation_thenReservedDecreasesAndAvailableIncreases() {
        stock.reserve(new BigDecimal("40"));
        stock.releaseReservation(new BigDecimal("15"));

        assertEquals(new BigDecimal("25"), stock.getReservedQuantity());
        assertEquals(new BigDecimal("75"), stock.getAvailableQuantity());
    }

    @Test
    void whenReleasingMoreThanReserved_thenThrowsIllegalState() {
        stock.reserve(new BigDecimal("10"));

        assertThrows(IllegalStateException.class, () -> stock.releaseReservation(new BigDecimal("20")));
    }

    @Test
    void whenReleasingZeroOrNegativeQuantity_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stock.releaseReservation(BigDecimal.ZERO));
    }

    @Test
    void whenConfirmingReservation_thenBothQuantityAndReservedDecrease() {
        stock.reserve(new BigDecimal("30"));
        stock.confirmReservation(new BigDecimal("30"));

        assertEquals(new BigDecimal("70"), stock.getQuantity());
        assertEquals(BigDecimal.ZERO, stock.getReservedQuantity());
        assertEquals(new BigDecimal("70"), stock.getAvailableQuantity());
    }

    @Test
    void whenConfirmingMoreThanReserved_thenThrowsIllegalState() {
        stock.reserve(new BigDecimal("10"));

        assertThrows(IllegalStateException.class, () -> stock.confirmReservation(new BigDecimal("20")));
    }

    @Test
    void whenConfirmingZeroOrNegativeQuantity_thenThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> stock.confirmReservation(BigDecimal.ZERO));
    }

    @Test
    void whenAvailableBelowMinimum_thenIsLowStockReturnsTrue() {
        stock.reserve(new BigDecimal("95"));

        assertTrue(stock.isLowStock());
    }

    @Test
    void whenAvailableAboveMinimum_thenIsLowStockReturnsFalse() {
        assertFalse(stock.isLowStock());
    }

    @Test
    void whenAvailableEqualsMinimum_thenIsLowStockReturnsTrue() {
        stock.reserve(new BigDecimal("90"));

        assertTrue(stock.isLowStock());
    }
}
