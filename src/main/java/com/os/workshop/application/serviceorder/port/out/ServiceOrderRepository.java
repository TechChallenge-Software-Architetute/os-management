package com.os.workshop.application.serviceorder.port.out;

import com.os.workshop.domain.serviceorder.ServiceOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository {
    ServiceOrder save(ServiceOrder order);
    Optional<ServiceOrder> findById(UUID id);
    List<ServiceOrder> findAll();
    List<ServiceOrder> findByCpfCnpj(String cpfCnpj);

    List<ServiceOrder> findActiveOrdersSorted();
}
