package com.os.workshop.features.product.supply.update;

import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSupplyHandler {

    private final SupplyRepository supplyRepository;

    @Transactional
    public Supply handle(Long id, UpdateSupplyRequest request) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with id: " + id));

        if (!supply.getSku().equals(request.sku()) && supplyRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A supply with SKU '" + request.sku() + "' already exists");
        }

        supply.setName(request.name());
        supply.setSku(request.sku());
        supply.setUnit(request.unit());
        supply.setCategory(request.category());
        supply.setBrand(request.brand());
        supply.setCostPrice(request.costPrice());
        supply.setSalePrice(request.salePrice());
        supply.setFractionalAllowed(request.fractionalAllowed());
        supply.setPackageSize(request.packageSize());
        return supplyRepository.save(supply);
    }
}
