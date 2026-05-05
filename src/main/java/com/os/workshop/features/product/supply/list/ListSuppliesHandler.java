package com.os.workshop.features.product.supply.list;

import com.os.workshop.features.product.shared.domain.Supply;
import com.os.workshop.features.product.shared.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSuppliesHandler {

    private final SupplyRepository supplyRepository;

    @Transactional(readOnly = true)
    public List<Supply> handle() {
        return supplyRepository.findAllActive();
    }
}
