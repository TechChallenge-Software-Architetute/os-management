package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindMyOrdersUseCase {

    private final ClientRepository clientRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional(readOnly = true)
    public List<ServiceOrder> execute(String clientIdentifier) {
        Client client = ClientLookup.resolve(clientRepository, clientIdentifier);
        return serviceOrderRepository.findByCpfCnpj(client.getDocument().getValue());
    }
}
