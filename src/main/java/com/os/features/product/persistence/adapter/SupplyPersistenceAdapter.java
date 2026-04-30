package com.os.features.product.persistence.adapter;

import com.os.features.product.domain.Supply;
import com.os.features.product.persistence.entity.SupplyEntity;
import com.os.features.product.persistence.mapper.SupplyMapper;
import com.os.features.product.persistence.repository.SupplyJpaRepository;
import com.os.features.product.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SupplyPersistenceAdapter implements SupplyRepository {

    private final SupplyJpaRepository jpaRepository;

    @Override
    public Supply save(Supply supply) {
        SupplyEntity entity;

        if (supply.getId() != null) {
            entity = jpaRepository.findById(supply.getId()).orElse(SupplyMapper.toEntity(supply));
            SupplyMapper.updateEntity(entity, supply);
        } else {
            entity = SupplyMapper.toEntity(supply);
        }

        return SupplyMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Supply> findById(UUID id) {
        return jpaRepository.findById(id).map(SupplyMapper::toDomain);
    }

    @Override
    public List<Supply> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
                .map(SupplyMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Supply> findBySku(String sku) {
        return jpaRepository.findBySku(sku).map(SupplyMapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }
}
