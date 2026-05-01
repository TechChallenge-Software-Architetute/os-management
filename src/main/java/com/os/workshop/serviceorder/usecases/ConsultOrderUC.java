package com.os.workshop.serviceorder.usecases;

import com.os.workshop.serviceorder.adapter.database.OrderRepository;
import com.os.workshop.serviceorder.domain.ServiceOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultOrderUC {

    @Autowired
    private OrderRepository orderRepository;

    public List<ServiceOrderEntity> process() {
        return orderRepository.findAll();
    }

    public ServiceOrderEntity process(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de servico nao encontrada. ID: " + id));
    }
}
