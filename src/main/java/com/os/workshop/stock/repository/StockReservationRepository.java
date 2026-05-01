package com.os.workshop.stock.repository;

import com.os.workshop.stock.domain.StockReservation;
import com.os.workshop.stock.domain.StockReservationStatus;

import java.util.List;
import java.util.Optional;

public interface StockReservationRepository {

    StockReservation save(StockReservation reservation);

    Optional<StockReservation> findById(Long id);

    List<StockReservation> findByServiceOrderId(Long serviceOrderId);

    List<StockReservation> findByServiceOrderIdAndStatus(Long serviceOrderId, StockReservationStatus status);
}
