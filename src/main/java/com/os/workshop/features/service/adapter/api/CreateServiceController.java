package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.CreateServiceRequest;
import com.os.workshop.features.service.usecases.CreateServiceUC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
public class CreateServiceController {

    @Autowired
    private CreateServiceUC createServiceUC;

    @PostMapping
    public ResponseEntity<ServiceEntity> createService(
            @RequestBody CreateServiceRequest request
    ) {
        try {

            var createdService = createServiceUC.process(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
