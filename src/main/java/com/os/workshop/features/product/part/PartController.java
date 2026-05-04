package com.os.workshop.features.product.part;

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

    @PostMapping
    public ResponseEntity<CreatePartResponse> create(@Valid @RequestBody CreatePartRequest request) {
        var part = createPartHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreatePartResponse.from(part));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindPartByIdResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FindPartByIdResponse.from(findPartByIdHandler.handle(id)));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<FindPartBySkuResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(FindPartBySkuResponse.from(findPartBySkuHandler.handle(sku)));
    }

    @GetMapping
    public ResponseEntity<List<ListPartsResponse>> findAll() {
        var parts = listPartsHandler.handle().stream().map(ListPartsResponse::from).toList();
        return ResponseEntity.ok(parts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdatePartResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePartRequest request) {
        return ResponseEntity.ok(UpdatePartResponse.from(updatePartHandler.handle(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivatePartHandler.handle(id);
        return ResponseEntity.noContent().build();
    }
}
