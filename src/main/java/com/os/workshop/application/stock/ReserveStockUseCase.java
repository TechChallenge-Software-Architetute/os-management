package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockMovementRepository;
import com.os.workshop.application.stock.port.out.StockRepository;
import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.stock.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReserveStockUseCase {

    private final StockRepository stockRepository;
    private final StockReservationRepository reservationRepository;
    private final StockMovementRepository movementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<StockReservation> execute(UUID serviceOrderId, List<ReserveItem> items) {
        List<ReservationContext> contexts = new ArrayList<>();

        for (ReserveItem item : items) {
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

        List<StockReservation> reservations = new ArrayList<>();
        for (ReservationContext ctx : contexts) {
            ctx.stock().reserve(ctx.quantity());
            stockRepository.save(ctx.stock());

            StockReservation reservation = new StockReservation();
            reservation.setStockId(ctx.stock().getId());
            reservation.setProductId(ctx.productId());
            reservation.setServiceOrderId(serviceOrderId);
            reservation.setQuantity(ctx.quantity());
            reservation.setStatus(StockReservationStatus.ACTIVE);
            reservations.add(reservationRepository.save(reservation));

            recordMovement(ctx.stock().getId(), StockMovementType.RESERVATION, ctx.quantity(),
                    "Reserved for OS: " + serviceOrderId);
        }

        eventPublisher.publishEvent(new ReservationChangedEvent(serviceOrderId));
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

    public record ReserveItem(Long productId, BigDecimal quantity) {}
    private record ReservationContext(Stock stock, Long productId, BigDecimal quantity) {}
}
