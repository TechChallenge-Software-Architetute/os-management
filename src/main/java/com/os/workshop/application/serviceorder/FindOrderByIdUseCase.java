package com.os.workshop.application.serviceorder;

import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindOrderByIdUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional(readOnly = true)
    public ServiceOrder execute(UUID id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de servico nao encontrada. ID: " + id));
    }
}
