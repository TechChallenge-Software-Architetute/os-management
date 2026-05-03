package com.os.workshop.features.product.supply;

import com.os.workshop.features.product.domain.ProductType;
import com.os.workshop.features.product.domain.Supply;
import com.os.workshop.features.product.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for Supply business rules and CRUD operations.
 * Supplies represent consumable products used in mechanic services (e.g., oil, lubricants).
 */
@Service
@RequiredArgsConstructor
public class SupplyService {

    private final SupplyRepository supplyRepository;

    /**
     * Creates a new supply after validating SKU uniqueness.
     *
     * @param request the supply data
     * @return the persisted supply with a generated ID
     * @throws IllegalArgumentException if the SKU is already in use
     */
    @Transactional
    public Supply create(SupplyRequest request) {
        if (supplyRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A supply with SKU '" + request.sku() + "' already exists");
        }

        Supply supply = new Supply();
        mapRequestToDomain(request, supply);
        supply.setType(ProductType.SUPPLY);
        return supplyRepository.save(supply);
    }

    /**
     * Finds a supply by its unique identifier.
     *
     * @param id the supply UUID
     * @return the supply domain object
     * @throws IllegalArgumentException if no supply exists with the given ID
     */
    @Transactional(readOnly = true)
    public Supply findById(Long id) {
        return supplyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with id: " + id));
    }

    /**
     * Finds a supply by its SKU code.
     *
     * @param sku the unique SKU identifier
     * @return the supply domain object
     * @throws IllegalArgumentException if no supply exists with the given SKU
     */
    @Transactional(readOnly = true)
    public Supply findBySku(String sku) {
        return supplyRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Supply not found with SKU: " + sku));
    }

    /**
     * Returns all active supplies. Deactivated supplies are excluded.
     *
     * @return list of active supplies
     */
    @Transactional(readOnly = true)
    public List<Supply> findAll() {
        return supplyRepository.findAllActive();
    }

    /**
     * Updates an existing supply. Validates SKU uniqueness if the SKU is being changed.
     *
     * @param id      the UUID of the supply to update
     * @param request the new supply data
     * @return the updated supply
     * @throws IllegalArgumentException if the supply is not found or the new SKU conflicts
     */
    @Transactional
    public Supply update(Long id, SupplyRequest request) {
        Supply supply = findById(id);

        if (!supply.getSku().equals(request.sku()) && supplyRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("A supply with SKU '" + request.sku() + "' already exists");
        }

        mapRequestToDomain(request, supply);
        return supplyRepository.save(supply);
    }

    /**
     * Soft-deletes a supply by setting it as inactive.
     * The supply remains in the database but is excluded from active listings.
     *
     * @param id the UUID of the supply to deactivate
     * @throws IllegalArgumentException if the supply is not found or already inactive
     */
    @Transactional
    public void deactivate(Long id) {
        Supply supply = findById(id);
        if (!supply.isActive()) {
            throw new IllegalArgumentException("Supply with id " + id + " is already inactive");
        }
        supply.setActive(false);
        supplyRepository.save(supply);
    }

    private void mapRequestToDomain(SupplyRequest request, Supply supply) {
        supply.setName(request.name());
        supply.setSku(request.sku());
        supply.setUnit(request.unit());
        supply.setCategory(request.category());
        supply.setBrand(request.brand());
        supply.setCostPrice(request.costPrice());
        supply.setSalePrice(request.salePrice());
        supply.setFractionalAllowed(request.fractionalAllowed());
        supply.setPackageSize(request.packageSize());
    }
}
