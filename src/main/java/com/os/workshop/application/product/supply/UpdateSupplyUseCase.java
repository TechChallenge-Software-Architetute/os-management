package com.os.workshop.application.product.supply;

import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.domain.product.Supply;
import com.os.workshop.domain.product.UnitOfMeasure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdateSupplyUseCase {

    private final SupplyRepository supplyRepository;

    @Transactional
    public Supply execute(Long id, String name, String sku, UnitOfMeasure unit, String category,
                          String brand, BigDecimal costPrice, BigDecimal salePrice,
                          boolean fractionalAllowed, BigDecimal packageSize) {
        Supply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with id: " + id));

        if (!supply.getSku().equals(sku) && supplyRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("A supply with SKU '" + sku + "' already exists");
        }

        supply.setName(name);
        supply.setSku(sku);
        supply.setUnit(unit);
        supply.setCategory(category);
        supply.setBrand(brand);
        supply.setCostPrice(costPrice);
        supply.setSalePrice(salePrice);
        supply.setFractionalAllowed(fractionalAllowed);
        supply.setPackageSize(packageSize);
        return supplyRepository.save(supply);
    }
}
