package com.os.workshop.features.product.supply.create;

import com.os.workshop.features.product.shared.domain.ProductType;
import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSupplyHandler {

    private final SupplyRepository supplyRepository;

    @Transactional
    public Supply handle(CreateSupplyRequest request) {
        if (supplyRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A supply with SKU '" + request.sku() + "' already exists");
        }

        Supply supply = new Supply();
        supply.setName(request.name());
        supply.setSku(request.sku());
        supply.setUnit(request.unit());
        supply.setCategory(request.category());
        supply.setBrand(request.brand());
        supply.setCostPrice(request.costPrice());
        supply.setSalePrice(request.salePrice());
        supply.setFractionalAllowed(request.fractionalAllowed());
        supply.setPackageSize(request.packageSize());
        supply.setType(ProductType.SUPPLY);
        return supplyRepository.save(supply);
    }
}
