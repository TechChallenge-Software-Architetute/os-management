package com.os.features.product.persistence.adapter;

import com.os.features.product.domain.Part;
import com.os.features.product.persistence.entity.PartEntity;
import com.os.features.product.persistence.mapper.PartMapper;
import com.os.features.product.persistence.repository.PartJpaRepository;
import com.os.features.product.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PartPersistenceAdapter implements PartRepository {

    private final PartJpaRepository jpaRepository;

    @Override
    public Part save(Part part) {
        PartEntity entity;

        if (part.getId() != null) {
            entity = jpaRepository.findById(part.getId()).orElse(PartMapper.toEntity(part));
            PartMapper.updateEntity(entity, part);
        } else {
            entity = PartMapper.toEntity(part);
        }

        return PartMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Part> findById(UUID id) {
        return jpaRepository.findById(id).map(PartMapper::toDomain);
    }

    @Override
    public List<Part> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
                .map(PartMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Part> findBySku(String sku) {
        return jpaRepository.findBySku(sku).map(PartMapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }
}
