package com.os.workshop.features.product.supply;

import com.os.workshop.features.product.supply.create.CreateSupplyHandler;
import com.os.workshop.features.product.supply.create.CreateSupplyRequest;
import com.os.workshop.features.product.supply.create.CreateSupplyResponse;
import com.os.workshop.features.product.supply.deactivate.DeactivateSupplyHandler;
import com.os.workshop.features.product.supply.findById.FindSupplyByIdHandler;
import com.os.workshop.features.product.supply.findById.FindSupplyByIdResponse;
import com.os.workshop.features.product.supply.findBySku.FindSupplyBySkuHandler;
import com.os.workshop.features.product.supply.findBySku.FindSupplyBySkuResponse;
import com.os.workshop.features.product.supply.list.ListSuppliesHandler;
import com.os.workshop.features.product.supply.list.ListSuppliesResponse;
import com.os.workshop.features.product.supply.update.UpdateSupplyHandler;
import com.os.workshop.features.product.supply.update.UpdateSupplyRequest;
import com.os.workshop.features.product.supply.update.UpdateSupplyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing supplies (consumable products).
 * Provides CRUD operations for supplies such as oils, lubricants, and other
 * consumables used in mechanic service orders.
 */
@Tag(name = "Supplies", description = "Manage consumable supplies.")
@Validated
@RestController
@RequestMapping("/api/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final CreateSupplyHandler createSupplyHandler;
    private final FindSupplyByIdHandler findSupplyByIdHandler;
    private final FindSupplyBySkuHandler findSupplyBySkuHandler;
    private final ListSuppliesHandler listSuppliesHandler;
    private final UpdateSupplyHandler updateSupplyHandler;
    private final DeactivateSupplyHandler deactivateSupplyHandler;

    @Operation(summary = "Create resource", description = "Create resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<CreateSupplyResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request payload for this operation",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateSupplyRequest.class))
            )
            @Valid @RequestBody CreateSupplyRequest request) {
        var supply = createSupplyHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateSupplyResponse.from(supply));
    }

    @Operation(summary = "Find resource by id", description = "Find resource by id endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FindSupplyByIdResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FindSupplyByIdResponse.from(findSupplyByIdHandler.handle(id)));
    }

    @Operation(summary = "Find resource by SKU", description = "Find resource by SKU endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<FindSupplyBySkuResponse> findBySku(
            @Pattern(regexp = "^[A-Za-z0-9]+[\\-_][A-Za-z0-9\\-_]+$", message = "SKU deve seguir o formato prefixo-sufixo (ex: BRK-PAD-001)")
            @PathVariable String sku) {
        return ResponseEntity.ok(FindSupplyBySkuResponse.from(findSupplyBySkuHandler.handle(sku)));
    }

    @Operation(summary = "List resources", description = "List resources endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ListSuppliesResponse>> findAll() {
        var supplies = listSuppliesHandler.handle().stream().map(ListSuppliesResponse::from).toList();
        return ResponseEntity.ok(supplies);
    }

    @Operation(summary = "Update resource", description = "Update resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UpdateSupplyResponse> update(@PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Request payload for this operation",
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateSupplyRequest.class))
    )
    @Valid @RequestBody UpdateSupplyRequest request) {
        return ResponseEntity.ok(UpdateSupplyResponse.from(updateSupplyHandler.handle(id, request)));
    }

    @Operation(summary = "Deactivate resource", description = "Deactivate resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateSupplyHandler.handle(id);
        return ResponseEntity.noContent().build();
    }
}
