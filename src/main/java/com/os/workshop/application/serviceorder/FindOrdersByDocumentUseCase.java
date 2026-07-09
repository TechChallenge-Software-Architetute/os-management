package com.os.workshop.application.serviceorder;

import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindOrdersByDocumentUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional(readOnly = true)
    public List<ServiceOrder> execute(String cpfCnpj) {
        String normalized = cpfCnpj == null ? "" : cpfCnpj.replaceAll("[^0-9]", "");
        return serviceOrderRepository.findByCpfCnpj(normalized);
    }
}
