package com.os.workshop.adapter.out.persistence.serviceorder;

import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.infrastructure.persistence.serviceorder.ServiceOrderEntity;
import com.os.workshop.infrastructure.persistence.serviceorder.ServiceOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceOrderPersistenceAdapter implements ServiceOrderRepository {

    private final ServiceOrderJpaRepository jpaRepository;

    @Override
    public ServiceOrder save(ServiceOrder order) {
        ServiceOrderEntity entity = toEntity(order);
        ServiceOrderEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ServiceOrder> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<ServiceOrder> findByCpfCnpj(String cpfCnpj) {
        return jpaRepository.findByCpfCnpj(cpfCnpj).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ServiceOrder> findActiveOrdersSorted() {
        return jpaRepository.findActiveOrdersSorted().stream().map(this::toDomain).toList();
    }

    private ServiceOrderEntity toEntity(ServiceOrder order) {
        ServiceOrderEntity entity = ServiceOrderEntity.builder()
                .id(order.getId())
                .serviceTypeName(order.getServiceTypeName())
                .serviceStatus(order.getServiceStatus())
                .listService(order.getListService())
                .cpfCnpj(order.getCpfCnpj())
                .placaVeiculo(order.getPlacaVeiculo())
                .rejectionReason(order.getRejectionReason())
                .build();
        if (order.getCreatedAt() != null) {
            entity.setCreatedAt(order.getCreatedAt());
        }
        if (order.getUpdatedAt() != null) {
            entity.setUpdatedAt(order.getUpdatedAt());
        }
        return entity;
    }

    private ServiceOrder toDomain(ServiceOrderEntity entity) {
        return ServiceOrder.reconstitute(
                entity.getId(),
                entity.getCpfCnpj(),
                entity.getPlacaVeiculo(),
                entity.getListService(),
                entity.getServiceTypeName(),
                OrderServiceStatusEnum.valueOf(entity.getServiceStatus()),
                entity.getRejectionReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
