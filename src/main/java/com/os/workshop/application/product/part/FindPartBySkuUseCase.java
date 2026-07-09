package com.os.workshop.application.product.part;

import com.os.workshop.application.product.part.port.out.PartRepository;
import com.os.workshop.domain.product.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindPartBySkuUseCase {

    private final PartRepository partRepository;

    @Transactional(readOnly = true)
    public Part execute(String sku) {
        return partRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with SKU: " + sku));
    }
}
