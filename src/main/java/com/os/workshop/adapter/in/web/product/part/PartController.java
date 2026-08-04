package com.os.workshop.adapter.in.web.product.part;

import com.os.workshop.application.product.part.*;
import com.os.workshop.domain.product.Part;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final CreatePartUseCase createPartUseCase;
    private final FindPartByIdUseCase findPartByIdUseCase;
    private final FindPartBySkuUseCase findPartBySkuUseCase;
    private final ListPartsUseCase listPartsUseCase;
    private final UpdatePartUseCase updatePartUseCase;
    private final DeactivatePartUseCase deactivatePartUseCase;

    @PostMapping
    public ResponseEntity<PartResponse> create(@Valid @RequestBody CreatePartRequest request) {
        Part part = createPartUseCase.execute(request.name(), request.sku(), request.unit(),
                request.category(), request.brand(), request.costPrice(), request.salePrice(),
                request.manufacturerCode(), request.warrantyMonths());
        return ResponseEntity.status(HttpStatus.CREATED).body(PartResponse.from(part));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(PartResponse.from(findPartByIdUseCase.execute(id)));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<PartResponse> findBySku(@PathVariable String sku) {
        return ResponseEntity.ok(PartResponse.from(findPartBySkuUseCase.execute(sku)));
    }

    @GetMapping
    public ResponseEntity<List<PartResponse>> findAll() {
        var parts = listPartsUseCase.execute().stream().map(PartResponse::from).toList();
        return ResponseEntity.ok(parts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePartRequest request) {
        Part part = updatePartUseCase.execute(id, request.name(), request.sku(), request.unit(),
                request.category(), request.brand(), request.costPrice(), request.salePrice(),
                request.manufacturerCode(), request.warrantyMonths());
        return ResponseEntity.ok(PartResponse.from(part));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        deactivatePartUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
