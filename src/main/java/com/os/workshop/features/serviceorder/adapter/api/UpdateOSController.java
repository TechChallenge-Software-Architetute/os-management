package com.os.workshop.features.serviceorder.adapter.api;

import com.os.workshop.features.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.domain.UpdateOrderRequest;
import com.os.workshop.features.serviceorder.usecases.UpdateOrderUC;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class UpdateOSController {

    private UpdateOrderUC updateOrderUC;

    @PatchMapping("/{id}")
    public ResponseEntity<ServiceOrderEntity> updateOrder(
            @PathVariable UUID id,
            @RequestBody UpdateOrderRequest request
    ) {
        try {
            var serviceOrder = updateOrderUC.process(id, request);

            return ResponseEntity.ok(serviceOrder);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
