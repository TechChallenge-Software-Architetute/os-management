package com.os.workshop.features.product.part.create;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.domain.ProductType;
import com.os.workshop.features.product.shared.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePartHandler {

    private final PartRepository partRepository;

    @Transactional
    public Part handle(CreatePartRequest request) {
        if (partRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A part with SKU '" + request.sku() + "' already exists");
        }

        Part part = new Part();
        part.setName(request.name());
        part.setSku(request.sku());
        part.setUnit(request.unit());
        part.setCategory(request.category());
        part.setBrand(request.brand());
        part.setCostPrice(request.costPrice());
        part.setSalePrice(request.salePrice());
        part.setManufacturerCode(request.manufacturerCode());
        part.setWarrantyMonths(request.warrantyMonths());
        part.setType(ProductType.PART);
        return partRepository.save(part);
    }
}
