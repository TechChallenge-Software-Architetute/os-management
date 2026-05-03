package com.os.workshop.features.stock.repository;

import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository {

    StockReservation save(StockReservation reservation);

    Optional<StockReservation> findById(Long id);

    List<StockReservation> findByServiceOrderId(UUID serviceOrderId);

    List<StockReservation> findByServiceOrderIdAndStatus(UUID serviceOrderId, StockReservationStatus status);
}
