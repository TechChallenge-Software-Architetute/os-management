package com.os.workshop.features.service.findByServiceOrder;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindServicesByServiceOrderHandler {

    private final ServiceRepository serviceRepository;

    public List<ServiceEntity> handle(UUID idOS) {
        return serviceRepository.findByIdOS(idOS);
    }
}
