package com.os.workshop.application.product.supply;

import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.domain.product.Supply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSuppliesUseCase {

    private final SupplyRepository supplyRepository;

    @Transactional(readOnly = true)
    public List<Supply> execute() {
        return supplyRepository.findAllActive();
    }
}
