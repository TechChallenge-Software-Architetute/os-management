package com.os.workshop.features.product.part.findById;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindPartByIdHandler {

    private final PartRepository partRepository;

    @Transactional(readOnly = true)
    public Part handle(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Part not found with id: " + id));
    }
}
