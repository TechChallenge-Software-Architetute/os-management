package com.os.workshop.features.product.part.update;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePartHandler {

    private final PartRepository partRepository;

    @Transactional
    public Part handle(Long id, UpdatePartRequest request) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with id: " + id));

        if (!part.getSku().equals(request.sku()) && partRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A part with SKU '" + request.sku() + "' already exists");
        }

        part.setName(request.name());
        part.setSku(request.sku());
        part.setUnit(request.unit());
        part.setCategory(request.category());
        part.setBrand(request.brand());
        part.setCostPrice(request.costPrice());
        part.setSalePrice(request.salePrice());
        part.setManufacturerCode(request.manufacturerCode());
        part.setWarrantyMonths(request.warrantyMonths());
        return partRepository.save(part);
    }
}
