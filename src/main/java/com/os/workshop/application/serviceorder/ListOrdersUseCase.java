package com.os.workshop.application.serviceorder;

import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListOrdersUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional(readOnly = true)
    public List<ServiceOrder> execute() {
        return serviceOrderRepository.findActiveOrdersSorted();
    }
}
