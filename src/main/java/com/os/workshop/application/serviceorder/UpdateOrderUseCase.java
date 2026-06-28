package com.os.workshop.application.serviceorder;

import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional
    public ServiceOrder execute(UUID id, OrderServiceStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("Status da ordem de servico e obrigatorio.");
        }

        var order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ordem de servico nao encontrada com id: " + id));

        order.setServiceStatus(status.getStatus());
        return serviceOrderRepository.save(order);
    }
}
