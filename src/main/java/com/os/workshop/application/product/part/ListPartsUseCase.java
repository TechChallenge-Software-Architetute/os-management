package com.os.workshop.application.product.part;

import com.os.workshop.application.product.part.port.out.PartRepository;
import com.os.workshop.domain.product.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPartsUseCase {

    private final PartRepository partRepository;

    @Transactional(readOnly = true)
    public List<Part> execute() {
        return partRepository.findAllActive();
    }
}
