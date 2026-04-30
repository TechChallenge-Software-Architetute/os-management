package com.os.workshop.serviceorder.adapter.database;

import com.os.workshop.serviceorder.domain.CreateOrderRequest;
import com.os.workshop.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.serviceorder.usecases.CreateOrderUC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class CreateOSController {

    @Autowired
    private CreateOrderUC createOrderUC;

    @PostMapping
    public ResponseEntity<ServiceOrderEntity> createService(
            @RequestBody CreateOrderRequest request
    ) {
        try {
            var serviceOrder = createOrderUC.process(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(serviceOrder);
        } catch (Exception e) {
            // Log the exception (not shown here for brevity)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
