package com.os.workshop.application.stock.port.out;

import com.os.workshop.domain.stock.StockReservation;
import com.os.workshop.domain.stock.StockReservationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository {
    StockReservation save(StockReservation reservation);
    Optional<StockReservation> findById(Long id);
    List<StockReservation> findByServiceOrderId(UUID serviceOrderId);
    List<StockReservation> findByServiceOrderIdAndStatus(UUID serviceOrderId, StockReservationStatus status);
}
