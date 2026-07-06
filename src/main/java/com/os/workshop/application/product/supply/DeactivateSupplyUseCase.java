package com.os.workshop.application.product.supply;

import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.domain.product.Supply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateSupplyUseCase {

    private final SupplyRepository supplyRepository;

    @Transactional
    public void execute(Long id) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with id: " + id));
        if (!supply.isActive()) {
            throw new IllegalArgumentException("Supply with id " + id + " is already inactive");
        }
        supply.setActive(false);
        supplyRepository.save(supply);
    }
}
