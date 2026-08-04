package com.os.workshop.application.service;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListServicesUseCase {

    private final ServiceRepository serviceRepository;

    public List<WorkshopService> execute() {
        return serviceRepository.findAll();
    }
}
