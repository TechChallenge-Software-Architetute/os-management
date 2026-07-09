package com.os.workshop.domain.stock;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    private Stock createStock(BigDecimal quantity, BigDecimal reserved, BigDecimal minimum) {
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setProductId(100L);
        stock.setQuantity(quantity);
        stock.setReservedQuantity(reserved);
        stock.setAvailableQuantity(quantity.subtract(reserved));
        stock.setMinimumQuantity(minimum);
        return stock;
    }

    // ==================== addQuantity ====================

    @Test
    void addQuantity_withPositiveAmount_increasesQuantityAndRecalculates() {
        Stock stock = createStock(new BigDecimal("50"), BigDecimal.ZERO, new BigDecimal("10"));

        stock.addQuantity(new BigDecimal("25"));

        assertEquals(new BigDecimal("75"), stock.getQuantity());
        assertEquals(new BigDecimal("75"), stock.getAvailableQuantity());
    }

    @Test
    void addQuantity_withPositiveAmount_andExistingReserved_recalculatesCorrectly() {
        Stock stock = createStock(new BigDecimal("50"), new BigDecimal("10"), new BigDecimal("5"));

        stock.addQuantity(new BigDecimal("20"));

        assertEquals(new BigDecimal("70"), stock.getQuantity());
        assertEquals(new BigDecimal("60"), stock.getAvailableQuantity());
    }

    @Test
    void addQuantity_withZero_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("50"), BigDecimal.ZERO, new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.addQuantity(BigDecimal.ZERO));
    }

    @Test
    void addQuantity_withNegativeAmount_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("50"), BigDecimal.ZERO, new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.addQuantity(new BigDecimal("-5")));
    }

    // ==================== removeQuantity ====================

    @Test
    void removeQuantity_withSufficientStock_decreasesQuantity() {
        Stock stock = createStock(new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));

        stock.removeQuantity(new BigDecimal("30"));

        assertEquals(new BigDecimal("70"), stock.getQuantity());
        assertEquals(new BigDecimal("70"), stock.getAvailableQuantity());
    }

    @Test
    void removeQuantity_withZero_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.removeQuantity(BigDecimal.ZERO));
    }

    @Test
    void removeQuantity_withNegativeAmount_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.removeQuantity(new BigDecimal("-10")));
    }

    @Test
    void removeQuantity_whenInsufficientAvailable_throwsIllegalState() {
        Stock stock = createStock(new BigDecimal("50"), new BigDecimal("40"), new BigDecimal("5"));
        // available = 50 - 40 = 10

        assertThrows(IllegalStateException.class, () -> stock.removeQuantity(new BigDecimal("15")));
    }

    @Test
    void removeQuantity_exactlyAvailable_succeeds() {
        Stock stock = createStock(new BigDecimal("50"), new BigDecimal("30"), new BigDecimal("5"));
        // available = 20

        stock.removeQuantity(new BigDecimal("20"));

        assertEquals(new BigDecimal("30"), stock.getQuantity());
        assertEquals(BigDecimal.ZERO, stock.getAvailableQuantity());
    }

    // ==================== reserve ====================

    @Test
    void reserve_withSufficientAvailable_increasesReservedAndDecreasesAvailable() {
        Stock stock = createStock(new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));

        stock.reserve(new BigDecimal("25"));

        assertEquals(new BigDecimal("25"), stock.getReservedQuantity());
        assertEquals(new BigDecimal("75"), stock.getAvailableQuantity());
        assertEquals(new BigDecimal("100"), stock.getQuantity());
    }

    @Test
    void reserve_withZero_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.reserve(BigDecimal.ZERO));
    }

    @Test
    void reserve_withNegativeAmount_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.reserve(new BigDecimal("-5")));
    }

    @Test
    void reserve_whenInsufficientAvailable_throwsIllegalState() {
        Stock stock = createStock(new BigDecimal("50"), new BigDecimal("45"), new BigDecimal("5"));
        // available = 5

        assertThrows(IllegalStateException.class, () -> stock.reserve(new BigDecimal("10")));
    }

    // ==================== releaseReservation ====================

    @Test
    void releaseReservation_withValidAmount_decreasesReservedAndIncreasesAvailable() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("10"));

        stock.releaseReservation(new BigDecimal("20"));

        assertEquals(new BigDecimal("10"), stock.getReservedQuantity());
        assertEquals(new BigDecimal("90"), stock.getAvailableQuantity());
    }

    @Test
    void releaseReservation_withZero_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.releaseReservation(BigDecimal.ZERO));
    }

    @Test
    void releaseReservation_withNegativeAmount_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.releaseReservation(new BigDecimal("-5")));
    }

    @Test
    void releaseReservation_moreThanReserved_throwsIllegalState() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("5"));

        assertThrows(IllegalStateException.class, () -> stock.releaseReservation(new BigDecimal("15")));
    }

    // ==================== confirmReservation ====================

    @Test
    void confirmReservation_withValidAmount_decreasesReservedAndQuantity() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("10"));

        stock.confirmReservation(new BigDecimal("20"));

        assertEquals(new BigDecimal("10"), stock.getReservedQuantity());
        assertEquals(new BigDecimal("80"), stock.getQuantity());
        assertEquals(new BigDecimal("70"), stock.getAvailableQuantity());
    }

    @Test
    void confirmReservation_withZero_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.confirmReservation(BigDecimal.ZERO));
    }

    @Test
    void confirmReservation_withNegativeAmount_throwsIllegalArgument() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class, () -> stock.confirmReservation(new BigDecimal("-5")));
    }

    @Test
    void confirmReservation_moreThanReserved_throwsIllegalState() {
        Stock stock = createStock(new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("5"));

        assertThrows(IllegalStateException.class, () -> stock.confirmReservation(new BigDecimal("15")));
    }

    // ==================== recalculateAvailableQuantity ====================

    @Test
    void recalculateAvailableQuantity_setsAvailableToQuantityMinusReserved() {
        Stock stock = new Stock();
        stock.setQuantity(new BigDecimal("80"));
        stock.setReservedQuantity(new BigDecimal("25"));

        stock.recalculateAvailableQuantity();

        assertEquals(new BigDecimal("55"), stock.getAvailableQuantity());
    }

    @Test
    void recalculateAvailableQuantity_whenNoReservations_availableEqualsQuantity() {
        Stock stock = new Stock();
        stock.setQuantity(new BigDecimal("100"));
        stock.setReservedQuantity(BigDecimal.ZERO);

        stock.recalculateAvailableQuantity();

        assertEquals(new BigDecimal("100"), stock.getAvailableQuantity());
    }

    // ==================== isLowStock ====================

    @Test
    void isLowStock_whenAvailableBelowMinimum_returnsTrue() {
        Stock stock = createStock(new BigDecimal("8"), BigDecimal.ZERO, new BigDecimal("10"));

        assertTrue(stock.isLowStock());
    }

    @Test
    void isLowStock_whenAvailableEqualsMinimum_returnsTrue() {
        Stock stock = createStock(new BigDecimal("10"), BigDecimal.ZERO, new BigDecimal("10"));

        assertTrue(stock.isLowStock());
    }

    @Test
    void isLowStock_whenAvailableAboveMinimum_returnsFalse() {
        Stock stock = createStock(new BigDecimal("50"), BigDecimal.ZERO, new BigDecimal("10"));

        assertFalse(stock.isLowStock());
    }
}
