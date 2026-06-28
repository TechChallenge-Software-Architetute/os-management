package com.os.workshop.adapter.out.persistence.product;

import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.domain.product.Supply;
import com.os.workshop.infrastructure.persistence.product.SupplyEntity;
import com.os.workshop.infrastructure.persistence.product.SupplyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupplyPersistenceAdapter implements SupplyRepository {

    private final SupplyJpaRepository jpaRepository;
    private final SupplyMapper supplyMapper;

    @Override
    public Supply save(Supply supply) {
        SupplyEntity entity;

        if (supply.getId() != null) {
            entity = jpaRepository.findById(supply.getId()).orElse(supplyMapper.toEntity(supply));
            supplyMapper.updateEntity(entity, supply);
        } else {
            entity = supplyMapper.toEntity(supply);
        }

        return supplyMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Supply> findById(Long id) {
        return jpaRepository.findById(id).map(supplyMapper::toDomain);
    }

    @Override
    public List<Supply> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
                .map(supplyMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Supply> findBySku(String sku) {
        return jpaRepository.findBySku(sku).map(supplyMapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }
}
