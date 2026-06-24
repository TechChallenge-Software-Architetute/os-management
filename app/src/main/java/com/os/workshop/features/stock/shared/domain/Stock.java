package com.os.workshop.features.stock.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    private Long id;
    private Long productId;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal reservedQuantity = BigDecimal.ZERO;
    private BigDecimal availableQuantity = BigDecimal.ZERO;
    private BigDecimal minimumQuantity = BigDecimal.ZERO;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Recalculates the available quantity based on current quantity and reserved quantity.
     * Must be called after any operation that changes quantity or reservedQuantity.
     */
    public void recalculateAvailableQuantity() {
        this.availableQuantity = this.quantity.subtract(this.reservedQuantity);
    }

    public boolean isLowStock() {
        return availableQuantity.compareTo(minimumQuantity) <= 0;
    }

    public void addQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        this.quantity = this.quantity.add(amount);
        recalculateAvailableQuantity();
    }

    public void removeQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }
        if (this.availableQuantity.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available stock. Available: " + this.availableQuantity);
        }
        this.quantity = this.quantity.subtract(amount);
        recalculateAvailableQuantity();
    }

    public void reserve(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        if (this.availableQuantity.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient available stock to reserve. Available: " + this.availableQuantity);
        }
        this.reservedQuantity = this.reservedQuantity.add(amount);
        recalculateAvailableQuantity();
    }

    public void releaseReservation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Release quantity must be positive");
        }
        if (this.reservedQuantity.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Cannot release more than reserved. Reserved: " + this.reservedQuantity);
        }
        this.reservedQuantity = this.reservedQuantity.subtract(amount);
        recalculateAvailableQuantity();
    }

    public void confirmReservation(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Confirm quantity must be positive");
        }
        if (this.reservedQuantity.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Cannot confirm more than reserved. Reserved: " + this.reservedQuantity);
        }
        this.reservedQuantity = this.reservedQuantity.subtract(amount);
        this.quantity = this.quantity.subtract(amount);
        recalculateAvailableQuantity();
    }
}
