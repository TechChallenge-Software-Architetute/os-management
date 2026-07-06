package com.os.workshop.application.product.part;

import com.os.workshop.application.product.part.port.out.PartRepository;
import com.os.workshop.domain.product.Part;
import com.os.workshop.domain.product.UnitOfMeasure;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdatePartUseCase {

    private final PartRepository partRepository;

    @Transactional
    public Part execute(Long id, String name, String sku, UnitOfMeasure unit, String category,
                        String brand, BigDecimal costPrice, BigDecimal salePrice,
                        String manufacturerCode, int warrantyMonths) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with id: " + id));

        if (!part.getSku().equals(sku) && partRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("A part with SKU '" + sku + "' already exists");
        }

        part.setName(name);
        part.setSku(sku);
        part.setUnit(unit);
        part.setCategory(category);
        part.setBrand(brand);
        part.setCostPrice(costPrice);
        part.setSalePrice(salePrice);
        part.setManufacturerCode(manufacturerCode);
        part.setWarrantyMonths(warrantyMonths);
        return partRepository.save(part);
    }
}
