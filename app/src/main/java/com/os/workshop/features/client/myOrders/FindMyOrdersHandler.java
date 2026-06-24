package com.os.workshop.features.client.myOrders;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Returns all service orders for the logged-in client.
 * Resolves the client from their email and looks up orders by CPF.
 */
@Service
@RequiredArgsConstructor
public class FindMyOrdersHandler {

    private final ClientRepository clientRepository;
    private final ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Transactional(readOnly = true)
    public List<ServiceOrderEntity> handle(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email: " + email));
        return serviceOrderJpaRepository.findByCpfCnpj(client.getCpf().getValue());
    }
}
