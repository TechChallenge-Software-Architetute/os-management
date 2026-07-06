package com.os.workshop.adapter.out.persistence.service;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.service.WorkshopService;
import com.os.workshop.infrastructure.persistence.service.ServiceEntity;
import com.os.workshop.infrastructure.persistence.service.ServiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServicePersistenceAdapter implements ServiceRepository {

    private final ServiceJpaRepository serviceJpaRepository;

    @Override
    public WorkshopService save(WorkshopService service) {
        ServiceEntity entity = toEntity(service);
        ServiceEntity saved = serviceJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<WorkshopService> findById(UUID id) {
        return serviceJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<WorkshopService> findByIdOS(UUID idOS) {
        return serviceJpaRepository.findByIdOS(idOS).stream().map(this::toDomain).toList();
    }

    @Override
    public List<WorkshopService> findAll() {
        return serviceJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private WorkshopService toDomain(ServiceEntity entity) {
        return new WorkshopService(entity.getId(), entity.getServiceTypeName(), entity.getIdOS(), entity.getServiceStatus());
    }

    private ServiceEntity toEntity(WorkshopService service) {
        return new ServiceEntity(service.getId(), service.getServiceTypeName(), service.getIdOS(), service.getServiceStatus());
    }
}
