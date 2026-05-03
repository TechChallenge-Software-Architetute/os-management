package com.os.workshop.features.service.usecases;

import com.os.workshop.features.service.adapter.database.ServiceRepository;
import com.os.workshop.features.service.domain.ServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindServiceByIdUC {

    @Autowired
    private ServiceRepository serviceRepository;

    public ServiceEntity process(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado. ID: " + id));
    }
}
