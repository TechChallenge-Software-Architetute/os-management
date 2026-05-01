package com.os.workshop.product.persistence.adapter;

import com.os.workshop.product.domain.Part;
import com.os.workshop.product.persistence.entity.PartEntity;
import com.os.workshop.product.persistence.mappers.PartMapper;
import com.os.workshop.product.persistence.repository.PartJpaRepository;
import com.os.workshop.product.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartPersistenceAdapter implements PartRepository {

    private final PartJpaRepository jpaRepository;
    private final PartMapper partMapper;

    @Override
    public Part save(Part part) {
        PartEntity entity;

        if (part.getId() != null) {
            entity = jpaRepository.findById(part.getId()).orElse(partMapper.toEntity(part));
            partMapper.updateEntity(entity, part);
        } else {
            entity = partMapper.toEntity(part);
        }

        return partMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Part> findById(Long id) {
        return jpaRepository.findById(id).map(partMapper::toDomain);
    }

    @Override
    public List<Part> findAllActive() {
        return jpaRepository.findByActiveTrue().stream()
                .map(partMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Part> findBySku(String sku) {
        return jpaRepository.findBySku(sku).map(partMapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }
}
