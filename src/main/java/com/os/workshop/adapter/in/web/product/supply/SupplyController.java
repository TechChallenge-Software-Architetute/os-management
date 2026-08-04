package com.os.workshop.adapter.in.web.product.supply;

import com.os.workshop.application.product.supply.*;
import com.os.workshop.domain.product.Supply;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplies")
@RequiredArgsConstructor
public class SupplyController {

    private final CreateSupplyUseCase createSupplyUseCase;
    private final FindSupplyByIdUseCase findSupplyByIdUseCase;
    private final FindSupplyBySkuUseCase findSupplyBySkuUseCase;
    private final ListSuppliesUseCase listSuppliesUseCase;
    private final UpdateSupplyUseCase updateSupplyUseCase;
    private final DeactivateSupplyUseCase deactivateSupplyUseCase;

    @PostMapping
    public ResponseEntity<SupplyResponse> create(@Valid @RequestBody CreateSupplyRequest request) {
        Supply supply = createSupplyUseCase.execute(request.name(), request.sku(), request.unit(),
                request.category(), request.brand(), request.costPrice(), request.salePrice(),
                request.fractionalAllowed(), request.packageSize());
        return ResponseEntity.status(HttpStatus.CREATED).body(SupplyResponse.from(supply));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplyResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(SupplyResponse.from(findSupplyByIdUseCase.execute(id)));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<SupplyResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(SupplyResponse.from(findSupplyBySkuUseCase.execute(sku)));
    }

    @GetMapping
    public ResponseEntity<List<SupplyResponse>> findAll() {
        var supplies = listSuppliesUseCase.execute().stream().map(SupplyResponse::from).toList();
        return ResponseEntity.ok(supplies);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplyResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateSupplyRequest request) {
        Supply supply = updateSupplyUseCase.execute(id, request.name(), request.sku(), request.unit(),
                request.category(), request.brand(), request.costPrice(), request.salePrice(),
                request.fractionalAllowed(), request.packageSize());
        return ResponseEntity.ok(SupplyResponse.from(supply));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivateSupplyUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
