package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.stock.StockReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindReservationsUseCase {

    private final StockReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<StockReservation> execute(UUID serviceOrderId) {
        return reservationRepository.findByServiceOrderId(serviceOrderId);
    }
}
