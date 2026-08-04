package com.os.workshop.application.product.part;

import com.os.workshop.application.product.part.port.out.PartRepository;
import com.os.workshop.domain.product.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivatePartUseCase {

    private final PartRepository partRepository;

    @Transactional
    public void execute(Long id) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with id: " + id));
        if (!part.isActive()) {
            throw new IllegalArgumentException("Part with id " + id + " is already inactive");
        }
        part.setActive(false);
        partRepository.save(part);
    }
}
