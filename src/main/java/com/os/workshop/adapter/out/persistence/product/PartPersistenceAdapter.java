package com.os.workshop.adapter.out.persistence.product;

import com.os.workshop.application.product.part.port.out.PartRepository;
import com.os.workshop.domain.product.Part;
import com.os.workshop.infrastructure.persistence.product.PartEntity;
import com.os.workshop.infrastructure.persistence.product.PartJpaRepository;
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
