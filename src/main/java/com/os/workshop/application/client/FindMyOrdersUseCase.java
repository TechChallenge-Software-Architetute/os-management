package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import com.os.workshop.domain.client.Cpf;
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
    public List<ServiceOrder> execute(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email: " + email));
        return serviceOrderRepository.findByCpfCnpj(client.getDocument().getValue());
    }

    /** Resolves the client by CPF/document — used for client (CPF) authentication. */
    @Transactional(readOnly = true)
    public List<ServiceOrder> executeByDocument(String document) {
        Client client = clientRepository.findByDocument(new Cpf(document).getValue())
                .orElseThrow(() -> new ClientNotFoundException("document: " + document));
        return serviceOrderRepository.findByCpfCnpj(client.getDocument().getValue());
    }
}
