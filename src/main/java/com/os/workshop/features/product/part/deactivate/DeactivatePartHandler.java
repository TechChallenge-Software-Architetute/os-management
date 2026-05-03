package com.os.workshop.features.product.part.deactivate;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivatePartHandler {

    private final PartRepository partRepository;

    @Transactional
    public void handle(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with id: " + id));
        if (!part.isActive()) {
            throw new IllegalArgumentException("Part with id " + id + " is already inactive");
        }
        part.setActive(false);
        partRepository.save(part);
    }
}
