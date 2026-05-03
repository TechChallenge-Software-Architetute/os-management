package com.os.workshop.features.product.supply.findById;

import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindSupplyByIdHandler {

    private final SupplyRepository supplyRepository;

    @Transactional(readOnly = true)
    public Supply handle(Long id) {
        return supplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with id: " + id));
    }
}
