package com.os.workshop.application.product.supply;

import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.domain.product.ProductType;
import com.os.workshop.domain.product.Supply;
import com.os.workshop.domain.product.UnitOfMeasure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreateSupplyUseCase {

    private final SupplyRepository supplyRepository;

    @Transactional
    public Supply execute(String name, String sku, UnitOfMeasure unit, String category,
                          String brand, BigDecimal costPrice, BigDecimal salePrice,
                          boolean fractionalAllowed, BigDecimal packageSize) {
        if (supplyRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("A supply with SKU '" + sku + "' already exists");
        }

        Supply supply = new Supply();
        supply.setName(name);
        supply.setSku(sku);
        supply.setUnit(unit);
        supply.setCategory(category);
        supply.setBrand(brand);
        supply.setCostPrice(costPrice);
        supply.setSalePrice(salePrice);
        supply.setFractionalAllowed(fractionalAllowed);
        supply.setPackageSize(packageSize);
        supply.setType(ProductType.SUPPLY);
        return supplyRepository.save(supply);
    }
}
