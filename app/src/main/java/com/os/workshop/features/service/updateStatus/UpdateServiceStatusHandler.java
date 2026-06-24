package com.os.workshop.features.service.updateStatus;

import com.os.workshop.features.service.shared.domain.Status;
import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateServiceStatusHandler {

    private final ServiceRepository serviceRepository;

    public ServiceEntity handle(UpdateServiceStatusRequest request) {

        var service = serviceRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado. ID: " + request.getId()));

        var statusList = service.getServiceStatus();

        statusList.add(new Status(request.getStatus(), LocalDateTime.now()));

        service.setServiceStatus(statusList);

        serviceRepository.save(service);

        return service;
    }
}
