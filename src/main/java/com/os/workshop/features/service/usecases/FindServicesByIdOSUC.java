package com.os.workshop.features.service.usecases;

import com.os.workshop.features.service.adapter.database.ServiceRepository;
import com.os.workshop.features.service.domain.ServiceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FindServicesByIdOSUC {

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceEntity> process(UUID idOS) {
        return serviceRepository.findByIdOS(idOS);
    }
}
