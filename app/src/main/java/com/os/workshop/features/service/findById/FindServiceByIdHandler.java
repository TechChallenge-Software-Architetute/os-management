package com.os.workshop.features.service.findById;

import com.os.workshop.features.service.shared.repository.ServiceEntity;
import com.os.workshop.features.service.shared.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindServiceByIdHandler {

    private final ServiceRepository serviceRepository;

    public ServiceEntity handle(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado. ID: " + id));
    }
}
