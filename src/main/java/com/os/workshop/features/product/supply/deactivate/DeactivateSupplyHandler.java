package com.os.workshop.features.product.supply.deactivate;

import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateSupplyHandler {

    private final SupplyRepository supplyRepository;

    @Transactional
    public void handle(Long id) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with id: " + id));
        if (!supply.isActive()) {
            throw new IllegalArgumentException("Supply with id " + id + " is already inactive");
        }
        supply.setActive(false);
        supplyRepository.save(supply);
    }
}
