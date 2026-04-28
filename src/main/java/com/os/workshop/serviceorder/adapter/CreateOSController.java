package com.os.workshop.serviceorder.adapter;

import com.os.workshop.serviceorder.usecases.CreateOrderUC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class CreateOSController {

    @Autowired
    private CreateOrderUC createOrderUC;

    @PostMapping
    public ResponseEntity<Void> createService(
            @RequestHeader(value = "correlationId") String correlationId
    ) {
        createOrderUC.process();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
