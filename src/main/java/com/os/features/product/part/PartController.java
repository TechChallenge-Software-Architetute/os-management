package com.os.features.product.part;

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
 * REST controller for managing automotive parts.
 * Provides CRUD operations for parts used in mechanic service orders.
 */
@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    /**
     * Creates a new part in the system.
     * The SKU must be unique across all parts. The ID is auto-generated.
     *
     * @param request the part data including name, SKU, unit, prices, manufacturer code and warranty
     * @return the created part with HTTP 201
     * @throws IllegalArgumentException if a part with the same SKU already exists
     */
    @PostMapping
    public ResponseEntity<PartResponse> create(@Valid @RequestBody PartRequest request) {
        var part = partService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PartResponse.from(part));
    }

    /**
     * Retrieves a part by its unique identifier.
     *
     * @param id the UUID of the part
     * @return the part data
     * @throws IllegalArgumentException if no part is found with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(PartResponse.from(partService.findById(id)));
    }

    /**
     * Retrieves a part by its SKU code.
     *
     * @param sku the unique SKU identifier
     * @return the part data
     * @throws IllegalArgumentException if no part is found with the given SKU
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<PartResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(PartResponse.from(partService.findBySku(sku)));
    }

    /**
     * Lists all active parts in the system.
     * Deactivated parts are excluded from the results.
     *
     * @return list of all active parts
     */
    @GetMapping
    public ResponseEntity<List<PartResponse>> findAll() {
        var parts = partService.findAll().stream().map(PartResponse::from).toList();
        return ResponseEntity.ok(parts);
    }

    /**
     * Updates an existing part.
     * All fields are replaced with the provided values. The SKU uniqueness is validated
     * if it differs from the current value.
     *
     * @param id      the UUID of the part to update
     * @param request the updated part data
     * @return the updated part
     * @throws IllegalArgumentException if the part is not found or the new SKU already exists
     */
    @PutMapping("/{id}")
    public ResponseEntity<PartResponse> update(@PathVariable Long id, @Valid @RequestBody PartRequest request) {
        return ResponseEntity.ok(PartResponse.from(partService.update(id, request)));
    }

    /**
     * Deactivates a part (soft delete).
     * The part is not physically removed from the database, but marked as inactive
     * and will no longer appear in active listings.
     *
     * @param id the UUID of the part to deactivate
     * @return HTTP 204 No Content on success
     * @throws IllegalArgumentException if the part is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        partService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
