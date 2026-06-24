package com.os.workshop.features.stock.findReservations;

import com.os.workshop.features.stock.shared.domain.StockReservation;
import com.os.workshop.features.stock.shared.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindReservationsHandler {

    private final StockReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<StockReservation> handle(UUID serviceOrderId) {
        return reservationRepository.findByServiceOrderId(serviceOrderId);
    }
}
