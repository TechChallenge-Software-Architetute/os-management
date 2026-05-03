package com.os.workshop.features.product.supply;

import com.os.workshop.features.common.api.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Supplies", description = "Manage consumable workshop supplies such as oils and lubricants.")
public class SupplyController {

    private static final Logger logger = LoggerFactory.getLogger(SupplyController.class);

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
    @Operation(summary = "Create supply", description = "Creates a new consumable supply. The SKU must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supply created successfully", content = @Content(schema = @Schema(implementation = SupplyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicated SKU", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplyResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Supply data to create.", required = true, content = @Content(schema = @Schema(implementation = SupplyRequest.class)))
            @Valid @RequestBody SupplyRequest request) {
        logger.info("Creating supply. sku={}", request.sku());
        var supply = supplyService.create(request);
        var response = SupplyResponse.from(supply);
        logger.info("Supply created. id={}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a supply by its unique identifier.
     *
     * @param id the UUID of the supply
     * @return the supply data
     * @throws IllegalArgumentException if no supply is found with the given ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Find supply by ID", description = "Retrieves an active consumable supply by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supply found", content = @Content(schema = @Schema(implementation = SupplyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid supply identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supply not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplyResponse> findById(
            @Parameter(description = "Supply unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Finding supply by id. id={}", id);
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
    @Operation(summary = "Find supply by SKU", description = "Retrieves an active consumable supply by its unique SKU.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supply found", content = @Content(schema = @Schema(implementation = SupplyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid SKU", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supply not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplyResponse> findBySku(
            @Parameter(description = "Supply unique SKU.", example = "SUP-OIL-5W30-001", required = true)
            @PathVariable String sku) {
        logger.info("Finding supply by SKU. sku={}", sku);
        return ResponseEntity.ok(SupplyResponse.from(supplyService.findBySku(sku)));
    }

    /**
     * Lists all active supplies in the system.
     * Deactivated supplies are excluded from the results.
     *
     * @return list of all active supplies
     */
    @GetMapping
    @Operation(summary = "List supplies", description = "Lists all active consumable supplies. Deactivated supplies are not returned.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplies listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SupplyResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<SupplyResponse>> findAll() {
        logger.info("Listing active supplies.");
        var supplies = supplyService.findAll().stream().map(SupplyResponse::from).toList();
        logger.info("Supplies listed. count={}", supplies.size());
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
    @Operation(summary = "Update supply", description = "Updates an existing consumable supply and validates SKU uniqueness when it changes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supply updated successfully", content = @Content(schema = @Schema(implementation = SupplyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicated SKU", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supply not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SupplyResponse> update(
            @Parameter(description = "Supply unique identifier.", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Supply data to update.", required = true, content = @Content(schema = @Schema(implementation = SupplyRequest.class)))
            @Valid @RequestBody SupplyRequest request) {
        logger.info("Updating supply. id={}", id);
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
    @Operation(summary = "Deactivate supply", description = "Soft-deletes a supply so it no longer appears in active listings.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supply deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid supply identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supply not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "Supply unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Deactivating supply. id={}", id);
        supplyService.deactivate(id);
        logger.info("Supply deactivated. id={}", id);
        return ResponseEntity.noContent().build();
    }
}
