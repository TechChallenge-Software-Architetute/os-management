package com.os.workshop.application.service;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindServicesByServiceOrderUseCase {

    private final ServiceRepository serviceRepository;

    public List<WorkshopService> execute(UUID idOS) {
        return serviceRepository.findByIdOS(idOS);
    }
}
