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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final CreateSupplyHandler createSupplyHandler;
    private final FindSupplyByIdHandler findSupplyByIdHandler;
    private final FindSupplyBySkuHandler findSupplyBySkuHandler;
    private final ListSuppliesHandler listSuppliesHandler;
    private final UpdateSupplyHandler updateSupplyHandler;
    private final DeactivateSupplyHandler deactivateSupplyHandler;

    @PostMapping
    public ResponseEntity<CreateSupplyResponse> create(@Valid @RequestBody CreateSupplyRequest request) {
        var supply = createSupplyHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateSupplyResponse.from(supply));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindSupplyByIdResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(FindSupplyByIdResponse.from(findSupplyByIdHandler.handle(id)));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<FindSupplyBySkuResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(FindSupplyBySkuResponse.from(findSupplyBySkuHandler.handle(sku)));
    }

    @GetMapping
    public ResponseEntity<List<ListSuppliesResponse>> findAll() {
        var supplies = listSuppliesHandler.handle().stream().map(ListSuppliesResponse::from).toList();
        return ResponseEntity.ok(supplies);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateSupplyResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateSupplyRequest request) {
        return ResponseEntity.ok(UpdateSupplyResponse.from(updateSupplyHandler.handle(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateSupplyHandler.handle(id);
        return ResponseEntity.noContent().build();
    }
}
