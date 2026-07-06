package com.os.workshop.adapter.in.web.service;

import com.os.workshop.application.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ServiceController {

    private final CreateServiceUseCase createServiceUseCase;
    private final FindServiceByIdUseCase findServiceByIdUseCase;
    private final FindServicesByServiceOrderUseCase findServicesByServiceOrderUseCase;
    private final ListServicesUseCase listServicesUseCase;
    private final ListServiceTypesUseCase listServiceTypesUseCase;
    private final UpdateServiceUseCase updateServiceUseCase;
    private final UpdateServiceStatusUseCase updateServiceStatusUseCase;

    @PostMapping("/services")
    public ResponseEntity<ServiceResponse> createService(@RequestBody CreateServiceRequest request) {
        try {
            var createdService = createServiceUseCase.execute(request.getServiceType(), request.getIdOS());
            return ResponseEntity.status(HttpStatus.CREATED).body(ServiceResponse.from(createdService));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> findById(@PathVariable UUID id) {
        try {
            var service = findServiceByIdUseCase.execute(id);
            return ResponseEntity.ok(ServiceResponse.from(service));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/services/os/{idOS}")
    public ResponseEntity<List<ServiceResponse>> findByIdOS(@PathVariable UUID idOS) {
        try {
            var services = findServicesByServiceOrderUseCase.execute(idOS).stream()
                    .map(ServiceResponse::from)
                    .toList();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> listServices() {
        try {
            var services = listServicesUseCase.execute().stream()
                    .map(ServiceResponse::from)
                    .toList();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/service-types")
    public ResponseEntity<List<ServiceTypeResponse>> listServiceTypes() {
        try {
            var serviceTypes = listServiceTypesUseCase.execute().stream()
                    .map(ServiceTypeResponse::from)
                    .toList();
            return ResponseEntity.ok(serviceTypes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> update(@PathVariable UUID id, @RequestBody UpdateServiceRequest request) {
        try {
            var service = updateServiceUseCase.execute(id, request.getServiceType(), request.getIdOS());
            return ResponseEntity.ok(ServiceResponse.from(service));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/services/update-status")
    public ResponseEntity<ServiceResponse> updateStatus(@RequestBody UpdateServiceStatusRequest request) {
        try {
            var service = updateServiceStatusUseCase.execute(request.getId(), request.getStatus());
            return ResponseEntity.ok(ServiceResponse.from(service));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
