package com.os.workshop.features.product.part.list;

import com.os.workshop.features.product.shared.domain.Part;
import com.os.workshop.features.product.shared.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPartsHandler {

    private final PartRepository partRepository;

    @Transactional(readOnly = true)
    public List<Part> handle() {
        return partRepository.findAllActive();
    }
}
