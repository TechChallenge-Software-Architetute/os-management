package com.os.workshop.features.product.part;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.Operation;

import com.os.workshop.features.product.part.create.CreatePartHandler;
import com.os.workshop.features.product.part.create.CreatePartRequest;
import com.os.workshop.features.product.part.create.CreatePartResponse;
import com.os.workshop.features.product.part.deactivate.DeactivatePartHandler;
import com.os.workshop.features.product.part.findById.FindPartByIdHandler;
import com.os.workshop.features.product.part.findById.FindPartByIdResponse;
import com.os.workshop.features.product.part.findBySku.FindPartBySkuHandler;
import com.os.workshop.features.product.part.findBySku.FindPartBySkuResponse;
import com.os.workshop.features.product.part.list.ListPartsHandler;
import com.os.workshop.features.product.part.list.ListPartsResponse;
import com.os.workshop.features.product.part.update.UpdatePartHandler;
import com.os.workshop.features.product.part.update.UpdatePartRequest;
import com.os.workshop.features.product.part.update.UpdatePartResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing automotive parts.
 * Provides CRUD operations for parts used in mechanic service orders.
 */
@Tag(name = "Parts", description = "Manage automotive parts.")
@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final CreatePartHandler createPartHandler;
    private final FindPartByIdHandler findPartByIdHandler;
    private final FindPartBySkuHandler findPartBySkuHandler;
    private final ListPartsHandler listPartsHandler;
    private final UpdatePartHandler updatePartHandler;
    private final DeactivatePartHandler deactivatePartHandler;

    @Operation(summary = "Create resource", description = "Create resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<CreatePartResponse> create(@io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Request payload for this operation",
        required = true,
        content = @Content(schema = @Schema(implementation = CreatePartRequest.class))
)
@Valid @RequestBody CreatePartRequest request) {
        var part = createPartHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreatePartResponse.from(part));
    }

    @Operation(summary = "Find resource by id", description = "Find resource by id endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FindPartByIdResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FindPartByIdResponse.from(findPartByIdHandler.handle(id)));
    }

    @Operation(summary = "Find resource by SKU", description = "Find resource by SKU endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<FindPartBySkuResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(FindPartBySkuResponse.from(findPartBySkuHandler.handle(sku)));
    }

    @Operation(summary = "List resources", description = "List resources endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<ListPartsResponse>> findAll() {
        var parts = listPartsHandler.handle().stream().map(ListPartsResponse::from).toList();
        return ResponseEntity.ok(parts);
    }

    @Operation(summary = "Update resource", description = "Update resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UpdatePartResponse> update(@PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(
         description = "Request payload for this operation",
         required = true,
         content = @Content(schema = @Schema(implementation = UpdatePartRequest.class))
 )
 @Valid @RequestBody UpdatePartRequest request) {
        return ResponseEntity.ok(UpdatePartResponse.from(updatePartHandler.handle(id, request)));
    }

    @Operation(summary = "Deactivate resource", description = "Deactivate resource endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivatePartHandler.handle(id);
        return ResponseEntity.noContent().build();
    }
}
