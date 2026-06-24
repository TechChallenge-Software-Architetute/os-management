package com.os.workshop.features.product.supply.findBySku;

import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindSupplyBySkuHandler {

    private final SupplyRepository supplyRepository;

    @Transactional(readOnly = true)
    public Supply handle(String sku) {
        return supplyRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with SKU: " + sku));
    }
}
