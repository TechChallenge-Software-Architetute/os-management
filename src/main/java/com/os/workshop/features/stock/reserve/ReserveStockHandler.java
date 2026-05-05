package com.os.workshop.features.stock.reserve;

import com.os.workshop.features.stock.shared.domain.*;
import com.os.workshop.features.stock.shared.repository.StockMovementRepository;
import com.os.workshop.features.stock.shared.repository.StockRepository;
import com.os.workshop.features.stock.shared.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReserveStockHandler {

    private final StockRepository stockRepository;
    private final StockReservationRepository reservationRepository;
    private final StockMovementRepository movementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<StockReservation> handle(ReserveStockRequest request) {
        // First pass: validate all items have enough available stock
        List<ReservationContext> contexts = new ArrayList<>();

        for (ReserveStockItemRequest item : request.items()) {
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

            reservations.add(reservationRepository.save(reservation));

            recordMovement(ctx.stock().getId(), StockMovementType.RESERVATION, ctx.quantity(),
                    "Reserved for OS: " + request.serviceOrderId());
        }

        eventPublisher.publishEvent(new ReservationChangedEvent(request.serviceOrderId()));
        return reservations;
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
