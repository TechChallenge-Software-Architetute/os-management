package com.os.workshop.features.product.supply;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing supplies (consumable products).
 * Provides CRUD operations for supplies such as oils, lubricants, and other
 * consumables used in mechanic service orders.
 */
@RestController
@RequestMapping("/api/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;

    /**
     * Creates a new supply in the system.
     * The SKU must be unique across all supplies. The ID is auto-generated.
     * Supplies can optionally allow fractional quantities (e.g., 3.5 liters of oil).
     *
     * @param request the supply data including name, SKU, unit, prices, fractional flag and package size
     * @return the created supply with HTTP 201
     * @throws IllegalArgumentException if a supply with the same SKU already exists
     */
    @PostMapping
    public ResponseEntity<SupplyResponse> create(@Valid @RequestBody SupplyRequest request) {
        var supply = supplyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SupplyResponse.from(supply));
    }

    /**
     * Retrieves a supply by its unique identifier.
     *
     * @param id the UUID of the supply
     * @return the supply data
     * @throws IllegalArgumentException if no supply is found with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplyResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(SupplyResponse.from(supplyService.findById(id)));
    }

    /**
     * Retrieves a supply by its SKU code.
     *
     * @param sku the unique SKU identifier
     * @return the supply data
     * @throws IllegalArgumentException if no supply is found with the given SKU
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<SupplyResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(SupplyResponse.from(supplyService.findBySku(sku)));
    }

    /**
     * Lists all active supplies in the system.
     * Deactivated supplies are excluded from the results.
     *
     * @return list of all active supplies
     */
    @GetMapping
    public ResponseEntity<List<SupplyResponse>> findAll() {
        var supplies = supplyService.findAll().stream().map(SupplyResponse::from).toList();
        return ResponseEntity.ok(supplies);
    }

    /**
     * Updates an existing supply.
     * All fields are replaced with the provided values. The SKU uniqueness is validated
     * if it differs from the current value.
     *
     * @param id      the UUID of the supply to update
     * @param request the updated supply data
     * @return the updated supply
     * @throws IllegalArgumentException if the supply is not found or the new SKU already exists
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplyResponse> update(@PathVariable Long id, @Valid @RequestBody SupplyRequest request) {
        return ResponseEntity.ok(SupplyResponse.from(supplyService.update(id, request)));
    }

    /**
     * Deactivates a supply (soft delete).
     * The supply is not physically removed from the database, but marked as inactive
     * and will no longer appear in active listings.
     *
     * @param id the UUID of the supply to deactivate
     * @return HTTP 204 No Content on success
     * @throws IllegalArgumentException if the supply is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        supplyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
