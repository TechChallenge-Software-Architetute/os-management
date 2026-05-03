package com.os.workshop.features.client.list;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Retorna todos os clientes ativos cadastrados no sistema.
 */
@Service
@RequiredArgsConstructor
public class ListClientsHandler {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<Client> handle() {
        return clientRepository.findAllActive();
    }
}
