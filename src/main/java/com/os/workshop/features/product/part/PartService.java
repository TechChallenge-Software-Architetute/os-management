package com.os.workshop.features.product.part;

import com.os.workshop.features.product.domain.Part;
import com.os.workshop.features.product.domain.ProductType;
import com.os.workshop.features.product.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for Part business rules and CRUD operations.
 * Parts represent physical components used in mechanic services (e.g., brake pads, filters).
 */
@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;

    /**
     * Creates a new part after validating SKU uniqueness.
     *
     * @param request the part data
     * @return the persisted part with a generated ID
     * @throws IllegalArgumentException if the SKU is already in use
     */
    @Transactional
    public Part create(PartRequest request) {
        if (partRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A part with SKU '" + request.sku() + "' already exists");
        }

        Part part = new Part();
        mapRequestToDomain(request, part);
        part.setType(ProductType.PART);
        return partRepository.save(part);
    }

    /**
     * Finds a part by its unique identifier.
     *
     * @param id the part UUID
     * @return the part domain object
     * @throws IllegalArgumentException if no part exists with the given ID
     */
    @Transactional(readOnly = true)
    public Part findById(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with id: " + id));
    }

    /**
     * Finds a part by its SKU code.
     *
     * @param sku the unique SKU identifier
     * @return the part domain object
     * @throws IllegalArgumentException if no part exists with the given SKU
     */
    @Transactional(readOnly = true)
    public Part findBySku(String sku) {
        return partRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with SKU: " + sku));
    }

    /**
     * Returns all active parts. Deactivated parts are excluded.
     *
     * @return list of active parts
     */
    @Transactional(readOnly = true)
    public List<Part> findAll() {
        return partRepository.findAllActive();
    }

    /**
     * Updates an existing part. Validates SKU uniqueness if the SKU is being changed.
     *
     * @param id      the UUID of the part to update
     * @param request the new part data
     * @return the updated part
     * @throws IllegalArgumentException if the part is not found or the new SKU conflicts
     */
    @Transactional
    public Part update(Long id, PartRequest request) {
        Part part = findById(id);

        if (!part.getSku().equals(request.sku()) && partRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A part with SKU '" + request.sku() + "' already exists");
        }

        mapRequestToDomain(request, part);
        return partRepository.save(part);
    }

    /**
     * Soft-deletes a part by setting it as inactive.
     * The part remains in the database but is excluded from active listings.
     *
     * @param id the UUID of the part to deactivate
     * @throws IllegalArgumentException if the part is not found or already inactive
     */
    @Transactional
    public void deactivate(Long id) {
        Part part = findById(id);
        if (!part.isActive()) {
            throw new IllegalArgumentException("Part with id " + id + " is already inactive");
        }
        part.setActive(false);
        partRepository.save(part);
    }

    private void mapRequestToDomain(PartRequest request, Part part) {
        part.setName(request.name());
        part.setSku(request.sku());
        part.setUnit(request.unit());
        part.setCategory(request.category());
        part.setBrand(request.brand());
        part.setCostPrice(request.costPrice());
        part.setSalePrice(request.salePrice());
        part.setManufacturerCode(request.manufacturerCode());
        part.setWarrantyMonths(request.warrantyMonths());
    }
}
