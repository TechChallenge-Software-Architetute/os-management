package com.os.workshop.stock.reservation;

import com.os.workshop.stock.domain.Stock;
import com.os.workshop.stock.domain.StockMovement;
import com.os.workshop.stock.domain.StockMovementType;
import com.os.workshop.stock.domain.StockReservation;
import com.os.workshop.stock.domain.StockReservationStatus;
import com.os.workshop.stock.repository.StockMovementRepository;
import com.os.workshop.stock.repository.StockRepository;
import com.os.workshop.stock.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for stock reservation business rules.
 * <p>
 * Reservations are created when a service order (OS) requires parts or supplies.
 * Reserved products remain physically in stock but are not available for other OS.
 * <p>
 * Reservation lifecycle:
 * <ul>
 *   <li>ACTIVE — products reserved, unavailable for other OS</li>
 *   <li>CONFIRMED — service completed, products consumed from stock</li>
 *   <li>RELEASED — OS cancelled, products returned to available pool</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class StockReservationService {

    private final StockRepository stockRepository;
    private final StockReservationRepository reservationRepository;
    private final StockMovementRepository movementRepository;

    /**
     * Reserves stock for all items in a service order (atomic operation).
     * <p>
     * First validates that every product has enough available stock, then reserves all of them.
     * If any product has insufficient stock, the entire operation fails and nothing is reserved.
     * This prevents partial reservations that could leave an OS without all required products.
     * <p>
     * Each reservation increases the {@code reservedQuantity} on the stock record,
     * reducing the computed {@code availableQuantity} for other service orders.
     *
     * @param request contains the service order ID and list of products with quantities
     * @return list of created reservations with ACTIVE status
     * @throws IllegalArgumentException if stock is not found for any product
     * @throws IllegalStateException    if any product has insufficient available stock
     */
    @Transactional
    public List<StockReservation> reserveForServiceOrder(StockReservationRequest request) {
        // First pass: validate all items have enough available stock
        List<ReservationContext> contexts = new ArrayList<>();

        for (StockReservationItemRequest item : request.items()) {
            Stock stock = stockRepository.findByProductId(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock not found for product: " + item.productId()));

            if (stock.getAvailableQuantity().compareTo(item.quantity()) < 0) {
                throw new IllegalStateException(
                        "Insufficient available stock for product " + item.productId()
                                + ". Available: " + stock.getAvailableQuantity()
                                + ", Requested: " + item.quantity());
            }

            contexts.add(new ReservationContext(stock, item.productId(), item.quantity()));
        }

        // Second pass: all validated, now reserve
        List<StockReservation> reservations = new ArrayList<>();

        for (ReservationContext ctx : contexts) {
            ctx.stock().reserve(ctx.quantity());
            stockRepository.save(ctx.stock());

            StockReservation reservation = new StockReservation();
            reservation.setStockId(ctx.stock().getId());
            reservation.setProductId(ctx.productId());
            reservation.setServiceOrderId(request.serviceOrderId());
            reservation.setQuantity(ctx.quantity());
            reservation.setStatus(StockReservationStatus.ACTIVE);

            StockReservation saved = reservationRepository.save(reservation);
            reservations.add(saved);

            recordMovement(ctx.stock().getId(), StockMovementType.RESERVATION, ctx.quantity(),
                    "Reserved for OS: " + request.serviceOrderId());
        }

        return reservations;
    }

    /**
     * Confirms all active reservations for a service order.
     * <p>
     * Called when the service is completed and products were actually consumed.
     * For each reservation, the reserved quantity is deducted from both
     * {@code reservedQuantity} and {@code quantity} on the stock record.
     * The reservation status changes from ACTIVE to CONFIRMED.
     *
     * @param serviceOrderId the UUID of the service order
     * @return list of confirmed reservations
     * @throws IllegalArgumentException if no active reservations exist for the OS
     */
    @Transactional
    public List<StockReservation> confirmReservations(UUID serviceOrderId) {
        List<StockReservation> activeReservations = reservationRepository
                .findByServiceOrderIdAndStatus(serviceOrderId, StockReservationStatus.ACTIVE);

        if (activeReservations.isEmpty()) {
            throw new IllegalArgumentException("No active reservations found for OS: " + serviceOrderId);
        }

        List<StockReservation> confirmed = new ArrayList<>();

        for (StockReservation reservation : activeReservations) {
            Stock stock = stockRepository.findById(reservation.getStockId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock not found for reservation: " + reservation.getId()));

            stock.confirmReservation(reservation.getQuantity());
            stockRepository.save(stock);

            reservation.setStatus(StockReservationStatus.CONFIRMED);
            confirmed.add(reservationRepository.save(reservation));

            recordMovement(stock.getId(), StockMovementType.RESERVATION_CONFIRMED, reservation.getQuantity(),
                    "Confirmed for OS: " + serviceOrderId);
        }

        return confirmed;
    }

    /**
     * Releases all active reservations for a service order.
     * <p>
     * Called when the OS is cancelled. The reserved quantities are returned to the
     * available pool by reducing {@code reservedQuantity} on the stock record.
     * The reservation status changes from ACTIVE to RELEASED.
     *
     * @param serviceOrderId the UUID of the service order
     * @return list of released reservations
     * @throws IllegalArgumentException if no active reservations exist for the OS
     */
    @Transactional
    public List<StockReservation> releaseReservations(UUID serviceOrderId) {
        List<StockReservation> activeReservations = reservationRepository
                .findByServiceOrderIdAndStatus(serviceOrderId, StockReservationStatus.ACTIVE);

        if (activeReservations.isEmpty()) {
            throw new IllegalArgumentException("No active reservations found for OS: " + serviceOrderId);
        }

        List<StockReservation> released = new ArrayList<>();

        for (StockReservation reservation : activeReservations) {
            Stock stock = stockRepository.findById(reservation.getStockId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock not found for reservation: " + reservation.getId()));

            stock.releaseReservation(reservation.getQuantity());
            stockRepository.save(stock);

            reservation.setStatus(StockReservationStatus.RELEASED);
            released.add(reservationRepository.save(reservation));

            recordMovement(stock.getId(), StockMovementType.RESERVATION_RELEASE, reservation.getQuantity(),
                    "Released from OS: " + serviceOrderId);
        }

        return released;
    }

    /**
     * Lists all reservations (any status) for a service order.
     *
     * @param serviceOrderId the UUID of the service order
     * @return list of all reservations for the OS
     */
    @Transactional(readOnly = true)
    public List<StockReservation> findByServiceOrderId(UUID serviceOrderId) {
        return reservationRepository.findByServiceOrderId(serviceOrderId);
    }

    private void recordMovement(Long stockId, StockMovementType type, BigDecimal quantity, String reason) {
        StockMovement movement = new StockMovement();
        movement.setStockId(stockId);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movementRepository.save(movement);
    }

    private record ReservationContext(Stock stock, Long productId, BigDecimal quantity) {}
}
