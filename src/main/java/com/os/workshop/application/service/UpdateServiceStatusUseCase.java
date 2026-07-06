package com.os.workshop.application.service;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateServiceStatusUseCase {

    private final ServiceRepository serviceRepository;

    public WorkshopService execute(UUID id, ServiceStatusEnum status) {
        var service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado. ID: " + id));

        var statusList = service.getServiceStatus();
        statusList.add(new Status(status, LocalDateTime.now()));
        service.setServiceStatus(statusList);

        return serviceRepository.save(service);
    }
}
