package com.os.workshop.features.product.part.findBySku;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindPartBySkuHandler {

    private final PartRepository partRepository;

    @Transactional(readOnly = true)
    public Part handle(String sku) {
        return partRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with SKU: " + sku));
    }
}
