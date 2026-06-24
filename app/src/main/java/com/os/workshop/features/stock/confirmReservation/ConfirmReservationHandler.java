package com.os.workshop.features.stock.confirmReservation;

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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmReservationHandler {

    private final StockRepository stockRepository;
    private final StockReservationRepository reservationRepository;
    private final StockMovementRepository movementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<StockReservation> handle(UUID serviceOrderId) {
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

        eventPublisher.publishEvent(new ReservationChangedEvent(serviceOrderId));
        return confirmed;
    }

    private void recordMovement(Long stockId, StockMovementType type, BigDecimal quantity, String reason) {
        StockMovement movement = new StockMovement();
        movement.setStockId(stockId);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movementRepository.save(movement);
    }
}
