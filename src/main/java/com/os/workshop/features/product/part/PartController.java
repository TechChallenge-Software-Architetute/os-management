package com.os.workshop.features.product.part;

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
 * REST controller for managing automotive parts.
 * Provides CRUD operations for parts used in mechanic service orders.
 */
@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
@Tag(name = "Parts", description = "Manage automotive parts used by workshop service orders.")
public class PartController {

    private static final Logger logger = LoggerFactory.getLogger(PartController.class);

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
    @Operation(summary = "Create part", description = "Creates a new automotive part. The SKU must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Part created successfully", content = @Content(schema = @Schema(implementation = PartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicated SKU", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Part data to create.", required = true, content = @Content(schema = @Schema(implementation = PartRequest.class)))
            @Valid @RequestBody PartRequest request) {
        logger.info("Creating part. sku={}", request.sku());
        var part = partService.create(request);
        var response = PartResponse.from(part);
        logger.info("Part created. id={}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a part by its unique identifier.
     *
     * @param id the UUID of the part
     * @return the part data
     * @throws IllegalArgumentException if no part is found with the given ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Find part by ID", description = "Retrieves an active automotive part by its unique identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Part found", content = @Content(schema = @Schema(implementation = PartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid part identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Part not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartResponse> findById(
            @Parameter(description = "Part unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Finding part by id. id={}", id);
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
    @Operation(summary = "Find part by SKU", description = "Retrieves an active automotive part by its unique SKU.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Part found", content = @Content(schema = @Schema(implementation = PartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid SKU", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Part not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartResponse> findBySku(
            @Parameter(description = "Part unique SKU.", example = "PART-OIL-FILTER-001", required = true)
            @PathVariable String sku) {
        logger.info("Finding part by SKU. sku={}", sku);
        return ResponseEntity.ok(PartResponse.from(partService.findBySku(sku)));
    }

    /**
     * Lists all active parts in the system.
     * Deactivated parts are excluded from the results.
     *
     * @return list of all active parts
     */
    @GetMapping
    @Operation(summary = "List parts", description = "Lists all active automotive parts. Deactivated parts are not returned.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parts listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PartResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PartResponse>> findAll() {
        logger.info("Listing active parts.");
        var parts = partService.findAll().stream().map(PartResponse::from).toList();
        logger.info("Parts listed. count={}", parts.size());
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
    @Operation(summary = "Update part", description = "Updates an existing automotive part and validates SKU uniqueness when it changes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Part updated successfully", content = @Content(schema = @Schema(implementation = PartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicated SKU", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Part not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PartResponse> update(
            @Parameter(description = "Part unique identifier.", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Part data to update.", required = true, content = @Content(schema = @Schema(implementation = PartRequest.class)))
            @Valid @RequestBody PartRequest request) {
        logger.info("Updating part. id={}", id);
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
    @Operation(summary = "Deactivate part", description = "Soft-deletes a part so it no longer appears in active listings.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Part deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid part identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Part not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "Part unique identifier.", example = "1", required = true)
            @PathVariable Long id) {
        logger.info("Deactivating part. id={}", id);
        partService.deactivate(id);
        logger.info("Part deactivated. id={}", id);
        return ResponseEntity.noContent().build();
    }
}
